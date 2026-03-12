def safe_filename(name):
    """Convert name to safe filename format"""
    safe = name.lower().replace(" ", "_").replace("/", "_").replace(":", "").replace("?", "").replace("&", "").replace("-", "_")
    safe = safe.replace("___", "_")
    return safe

name = "Jack-o'-Lantern"
result = safe_filename(name)
print(f"Input: {name}")
print(f"Output: {result}")
