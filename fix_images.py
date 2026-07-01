import sys, json, requests

# Fetch items with broken picsum URLs
API = "http://localhost:8080/api"

# Get all items page by page
items = []
page = 1
while True:
    resp = requests.get(f"{API}/items?page={page}&size=50")
    data = resp.json()
    records = data.get('records', [])
    if not records:
        break
    items.extend(records)
    page += 1

# Real image URLs by type (Unsplash free images)
cover_images = {
    'coffee': [
        "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=600&h=400&fit=crop",
        "https://images.unsplash.com/photo-1497935586351-b67a49e012bf?w=600&h=400&fit=crop",
        "https://images.unsplash.com/photo-1512568400610-62d28a2a7c72?w=600&h=400&fit=crop",
        "https://images.unsplash.com/photo-1504630083234-14187a9df0f5?w=600&h=400&fit=crop",
        "https://images.unsplash.com/photo-1559056199-641a0ac8b55e?w=600&h=400&fit=crop",
        "https://images.unsplash.com/photo-1514432324607-a09d9b4aefda?w=600&h=400&fit=crop",
        "https://images.unsplash.com/photo-1572442388796-11668a67e53d?w=600&h=400&fit=crop",
        "https://images.unsplash.com/photo-1568649929103-28ffbefaca1e?w=600&h=400&fit=crop",
    ],
    'anime': [
        "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=600&h=400&fit=crop",
        "https://images.unsplash.com/photo-1618336753974-aae8e04506aa?w=600&h=400&fit=crop",
        "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?w=600&h=400&fit=crop",
        "https://images.unsplash.com/photo-1581833971358-2c8b77f1b581?w=600&h=400&fit=crop",
    ],
    'offline': [
        "https://images.unsplash.com/photo-1561214115-f2f134cc4912?w=600&h=400&fit=crop",
        "https://images.unsplash.com/photo-1470229722913-7c0e2dbbafd3?w=600&h=400&fit=crop",
        "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=600&h=400&fit=crop",
    ],
    'game': [
        "https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=600&h=400&fit=crop",
    ],
}

idx = {'coffee': 0, 'anime': 0, 'offline': 0, 'game': 0}
updated = 0
session = requests.Session()
# Use admin auth - get a token first
login = session.post(f"{API}/auth/login", json={"username": "admin", "password": "changeme"})
token = login.json().get("token")
session.headers.update({"Authorization": f"Bearer {token}"})


for item in items:
    url = item.get('coverUrl') or ''
    if 'picsum' not in url:
        continue
    t = item['type']
    pool = cover_images.get(t, cover_images['game'])
    new_url = pool[idx[t] % len(pool)]
    idx[t] += 1
    
    # Update via admin API
    resp = session.put(f"{API}/admin/items/{item['id']}", json={"coverUrl": new_url})
    if resp.status_code == 200:
        updated += 1
        print(f"  ✅ {t} id={item['id']} {item['title'][:25]}")
    else:
        print(f"  ❌ {t} id={item['id']} {resp.status_code}: {resp.text[:50]}")

print(f"\n共更新 {updated} 条封面图片")
