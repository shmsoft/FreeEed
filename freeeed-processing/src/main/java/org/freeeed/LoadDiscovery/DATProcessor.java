package org.freeeed.LoadDiscovery;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.tika.metadata.Metadata;
import org.freeeed.data.index.SolrIndex;
import org.freeeed.main.DocumentMetadata;
import org.freeeed.main.FreeEedMain;
import org.freeeed.main.ZipFileWriter;
import org.freeeed.services.Project;
import org.freeeed.services.UniqueIdGenerator;
import org.freeeed.services.Util;
import org.freeeed.util.LogFactory;
import org.freeeed.util.OsUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class DATProcessor implements LoadDiscoveryFile {
    private final Project project = Project.getCurrentProject();
    private final static java.util.logging.Logger LOGGER = LogFactory.getLogger(DATProcessor.class.getName());
    protected ZipFileWriter zipFileWriter = new ZipFileWriter();

    // DAT dialects we recognize. A standard Concordance/Relativity DAT uses 0x14
    // as the field separator and 0xFE (thorn) as the text qualifier; exports also
    // come tab- or pipe-delimited, with a double-quote or no qualifier. We detect
    // these from the header rather than assuming one -- picking the wrong delimiter
    // leaves every row unsplit (issues #519, #599).
    private static final char[] CANDIDATE_DELIMS = { (char) 0x14, '\t', '|' };
    private static final char[] CANDIDATE_QUALS  = { (char) 0xFE, '"' };

    @Override
    public void processLoadFile() {
        zipFileWriter.setup();
        try {
            zipFileWriter.openZipForWriting();
        } catch (IOException e) {
            e.printStackTrace();
        }

        SolrIndex.getInstance().init();
        List<String> inputs = Arrays.asList(project.getInputs());
        inputs.forEach(temp -> {
            String textFileName = null;
            if (!FilenameUtils.getExtension(temp).equals("dat")) {
                return;
            }
            try {
                List<String> lines = FileUtils.readLines(new File(temp), "UTF-8");
                if (lines.isEmpty()) {
                    return;
                }
                // Auto-detect the field delimiter + text qualifier from the header
                // instead of hardcoding Concordance 0x14/0xFE.
                String headerLine = stripBom(lines.get(0));
                char delim = detectDelimiter(headerLine);
                char qual = detectQualifier(headerLine);
                LOGGER.info("DAT " + new File(temp).getName() + ": field delimiter 0x"
                        + Integer.toHexString(delim)
                        + (qual != 0 ? ", qualifier 0x" + Integer.toHexString(qual) : ", no qualifier"));
                String[] titleParts = splitLine(headerLine, delim, qual);
                for (int i = 1; i < lines.size(); i++) {
                    String[] lineParts = splitLine(lines.get(i), delim, qual);
                    Metadata m = new Metadata();
                    for (int j = 0; j < lineParts.length && j < titleParts.length; j++) {
                        String p = lineParts[j];
                        if (titleParts[j].equals("EXTRACTED TEXT")) {
                            textFileName = p;
                        }
                        m.set(titleParts[j], p);
                    }
                    m.set("UPI", UniqueIdGenerator.getInstance().getNextId());
                    File f;
                    if (textFileName != null) {
                        /**
                         * In linux we have to change \ to / for review to work
                         */
                        if (!OsUtil.isWindows()) {
                            textFileName = textFileName.replace("\\", "/");
                            f = new File(textFileName);
                        } else {
                            f = new File(textFileName);
                        }

                        LOGGER.info("Reading: " + f.getName());
                        List<File> files = (List<File>) FileUtils.listFiles(
                                new File(project.getStagingDir()),
                                new RegexFileFilter(f.getName()),
                                DirectoryFileFilter.DIRECTORY
                        );
                        String text = FileUtils.readFileToString(files.get(0), StandardCharsets.UTF_8);
                        text = text.replaceAll("\r", "<br>").replaceAll("\n", "<br>");
                        text = text.replaceAll("[\\x00-\\x09\\x11\\x12\\x14-\\x1F\\x7F]", "");
                        text = Util.removeNonUtf8CompliantCharacters(text);
                        m.set(DocumentMetadata.DOCUMENT_TEXT, text);
                        m.set("text_link", f.getName());
                        zipFileWriter.addTextFile(f.getName(), text);
                    }
                    SolrIndex.getInstance().addBatchData(m);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        try {
            zipFileWriter.closeZip();
        } catch (IOException e) {
            e.printStackTrace();
        }
        SolrIndex.getInstance().flushBatchData();
        SolrIndex.getInstance().destroy();
    }

    /** Strip a leading UTF-8 BOM if the decoder left it on the first line. */
    private static String stripBom(String s) {
        return (s != null && !s.isEmpty() && s.charAt(0) == (char) 0xFEFF) ? s.substring(1) : s;
    }

    /** Pick the field delimiter that appears most in the header; default 0x14. */
    private static char detectDelimiter(String header) {
        char best = (char) 0x14;
        int bestCount = 0;
        for (char d : CANDIDATE_DELIMS) {
            int c = countChar(header, d);
            if (c > bestCount) {
                bestCount = c;
                best = d;
            }
        }
        return best;
    }

    /** Detect the text qualifier present in the header, or 0 if none. */
    private static char detectQualifier(String header) {
        for (char q : CANDIDATE_QUALS) {
            if (header.indexOf(q) >= 0) {
                return q;
            }
        }
        return 0;
    }

    /** Strip the qualifier (if any) and split the line on the detected delimiter. */
    private static String[] splitLine(String line, char delim, char qual) {
        if (line == null) {
            return new String[0];
        }
        line = stripBom(line);
        if (qual != 0) {
            line = line.replace(String.valueOf(qual), "");
        }
        return line.split(Pattern.quote(String.valueOf(delim)), -1);
    }

    private static int countChar(String s, char c) {
        int n = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) {
                n++;
            }
        }
        return n;
    }
}
