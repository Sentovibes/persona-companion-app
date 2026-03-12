import undetected_chromedriver as uc
import time

print("Testing Megaten Wiki API access...")
print("Opening Chrome browser (visible)...")

# Initialize Chrome (visible, not headless)
driver = uc.Chrome(use_subprocess=True, version_main=145)

try:
    # Test the API
    url = "https://megatenwiki.com/api.php?action=parse&page=Glorious_Hand&prop=images&format=json"
    print(f"\nNavigating to: {url}")
    
    driver.get(url)
    time.sleep(5)  # Wait for page to load
    
    page_source = driver.page_source
    
    if "403" in page_source:
        print("\n❌ 403 ERROR - Still blocked!")
    elif "Just a moment" in page_source:
        print("\n⚠️  Cloudflare challenge detected - waiting...")
        time.sleep(10)  # Wait for challenge to complete
        page_source = driver.page_source
        if "parse" in page_source:
            print("✅ Challenge passed! API accessible")
        else:
            print("❌ Challenge failed")
    elif "parse" in page_source or "{" in page_source:
        print("\n✅ SUCCESS! API is accessible")
        print(f"\nFirst 500 chars of response:\n{page_source[:500]}")
    else:
        print(f"\n⚠️  Unexpected response:\n{page_source[:500]}")
    
    input("\nPress Enter to close browser...")
    
finally:
    driver.quit()
    print("\nBrowser closed.")
