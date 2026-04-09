from pathlib import Path
imgs = {p.stem for p in Path("../app/src/main/assets/images/personas_shared").glob("*.png")}

names = ["Ame-no-Uzume", "Jack-o'-Lantern", "Arsene", "Izanagi-no-Okami",
         "Fuu-Ki", "Sui-Ki", "Kin-Ki", "Kikuri-Hime", "Hitokoto-Nushi",
         "Yamata-no-Orochi", "Yomotsu-Shikome", "Yomotsu-Ikusa"]

def old_path(name):
    return (name.lower()
        .replace(" ","_").replace("-","_").replace("/","_")
        .replace(":","").replace("?","").replace("'","").replace("\u2019","").replace("&",""))

def new_path(name):
    return (name.lower()
        .replace("è","e").replace("é","e").replace("ā","a")
        .replace("ō","o").replace("ū","u").replace("î","i")
        .replace(" ","_").replace("/","_")
        .replace(":","").replace("?","").replace("'","").replace("\u2019","").replace("&",""))

print(f"{'Name':<30} {'Old':>4} {'New':>4}")
print("-"*45)
for name in names:
    o = old_path(name)
    n = new_path(name)
    print(f"{name:<30} {'OK' if o in imgs else 'MISS':>4} {'OK' if n in imgs else 'MISS':>4}  ({n})")
