import json, requests, re

def clean_req(text):
    if not text: return ''
    s = re.sub(r'</li>', '\n', text)
    s = re.sub(r'<br\s*/?>', '\n', s)
    s = re.sub(r'<li>', '• ', s)
    s = re.sub(r'<strong>', '', s)
    s = re.sub(r'</strong>', ' ', s)
    s = re.sub(r'<[^>]+>', '', s)
    s = s.replace('&nbsp;',' ').replace('&lt;','<').replace('&gt;','>')
    s = re.sub(r'\n{2,}', '\n', s).strip()
    return s

appids = [(576,'2582320'),(577,'2357570'),(578,'374320'),
          (581,'2358720'),(582,'236430'),(583,'2138330'),
          (584,'322330'),(585,'219740'),(586,'2239770'),
          (587,'712640'),(588,'393010'),(589,'282470')]

with open('steam_fix.sql', 'w', encoding='utf-8') as f:
    f.write('SET NAMES utf8mb4;\n')
    for db_id, appid in appids:
        r = requests.get(f'https://store.steampowered.com/api/appdetails?appids={appid}&cc=cn&l=schinese')
        j = r.json()
        data = j.get(str(appid), {}).get('data', {})
        if not data: continue
        
        rd = (data.get('release_date',{}) or {}).get('date','')
        lang = re.sub(r'<[^>]+>', '', data.get('supported_languages','')).replace('  ',' ').strip()
        po = data.get('price_overview',{})
        price = f"{po['currency']} {po['final']/100:.2f}" if po else ''
        
        min_r = clean_req((data.get('pc_requirements',{}) or {}).get('minimum',''))
        rec_r = clean_req((data.get('pc_requirements',{}) or {}).get('recommended',''))
        
        sql = f"""UPDATE items SET info_json = JSON_SET(
            info_json,
            '$.release_date', '{rd}',
            '$.languages', '{lang}',
            '$.price', '{price}',
            '$.min_requirements', '{min_r}',
            '$.rec_requirements', '{rec_r}'
        ) WHERE id = {db_id};
"""
        f.write(sql)
        print(f'  id={db_id}: min={min_r[:40] if min_r else "N/A"}')

print('\nDone')
