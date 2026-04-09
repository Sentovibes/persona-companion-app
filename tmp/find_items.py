import os

root_dir = r"C:\Users\omare\Music\Persona-Companion-App\EXTRA DB"
results = []

for root, dirs, files in os.walk(root_dir):
    for file in files:
        if "item" in file.lower() or file.lower().endswith(".txt"):
            results.append(os.path.join(root, file))

print(f"Found {len(results)} potential files:")
for path in results[:100]:  # Limit to 100
    print(path)
