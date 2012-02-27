package org.freeeed.print;

import java.io.File;
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.OfficeManager;
import org.freeeed.main.Map;
import org.freeeed.services.FreeEedUtil;

public class OfficePrint {

    public static void createPdf(String officeDocFile, String outputPdf) {
        String extension = FreeEedUtil.getExtension(officeDocFile);
        if ("html".equalsIgnoreCase(extension) || "htm".equalsIgnoreCase(extension)) {
            Html2Pdf.html2pdf(officeDocFile, outputPdf);
        } else {
            OfficeManager officeManager = Map.getOfficeManager();
            OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);
            converter.convert(new File(officeDocFile), new File(outputPdf));
        }
    }
}
