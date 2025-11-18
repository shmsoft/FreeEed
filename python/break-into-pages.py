def break_into_pages(filename):
    with open(filename, 'r') as file:
        content = file.read()

    # Split by form feed character
    pages = content.split('\f')

    return pages


# Usage
filename = 'your_text_file.txt'
pages = break_into_pages(filename)

# Now `pages` is a list of strings, each string representing a page of processed
for i, page in enumerate(pages):
    print(f"Page {i + 1}:\n{page}\n{'-' * 50}\n")