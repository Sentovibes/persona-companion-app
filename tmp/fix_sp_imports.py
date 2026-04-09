import os
import re

screens_dir = "app/src/main/java/com/persona/companion/ui/screens"

for root, dirs, files in os.walk(screens_dir):
    for file in files:
        if file.endswith(".kt"):
            path = os.path.join(root, file)
            with open(path, "r", encoding="utf-8") as f:
                content = f.read()
            
            # Check if .sp is used but the import is missing
            if ".sp" in content and "import androidx.compose.ui.unit.sp" not in content:
                print(f"Adding sp import to {path}")
                # Try to add it after the dp import
                new_content = re.sub(
                    r"(import androidx\.compose\.ui\.unit\.dp)",
                    r"\1\nimport androidx.compose.ui.unit.sp",
                    content
                )
                # If dp import wasn't found, try to add it after any package/import
                if new_content == content:
                    new_content = re.sub(
                        r"(package [a-zA-Z0-9.]+)",
                        r"\1\n\nimport androidx.compose.ui.unit.sp",
                        content
                    )
                
                with open(path, "w", encoding="utf-8") as f:
                    f.write(new_content)
