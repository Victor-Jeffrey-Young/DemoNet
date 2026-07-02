import json, requests, re, subprocess

def clean_lang(lang):
    lang = re.sub(r'<[^>]+>', '', lang).replace('  ',' ').strip()
    return lang

def clean_html(html):
    return re.sub(r'<[^>]+>', '', html).replace('&nbsp;',' ').strip()

appids = [
    (576, '2582320'), (577, '2357570'), (578, '374320'),
    (581, '2358720'), (582, '236430'), (583, '2138330'),
    (584, '322330'), (585, '219740'), (586, '2239770'),
    (587, '712640'), (588, '393010'), (589, '282470'),
]

for db_id, appid in appids:
    r = requests.get(f'https://store.steampowered.com/api/appdetails?appids={appid}&cc=cn&l=schinese')
    data = r.json().get(appid, {}).get('data', {})
    if not data:
        print(f'FAIL id={db_id} appid={appid}')
        continue
    
    rd = data.get('release_date',{}).get('date','')
    lang = clean_lang(data.get('supported_languages',''))
    po = data.get('price_overview',{})
    price = f"{po['currency']} {po['final']/100:.2f}" if po else ''
    min_req = clean_html(data.get('pc_requirements',{}).get('minimum',''))
    rec_req = clean_html(data.get('pc_requirements',{}).get('recommended',''))
    
    sql = f"""UPDATE items SET info_json = JSON_SET(
        info_json,
        '$.release_date', '{rd.replace("'","''")}',
        '$.languages', '{lang.replace("'","''")}',
        '$.price', '{price.replace("'","''")}',
        '$.min_requirements', '{min_req.replace("'","''")}',
        '$.rec_requirements', '{rec_req.replace("'","''")}'
    ) WHERE id = {db_id};"""
    
    result = subprocess.run(
        ['docker', 'exec', '-i', 'demonet-mysql', 'mysql', '-uroot', '-pchangeme', 'demonet'],
        input=sql, capture_output=True, text=True
    )
    ok = result.returncode == 0
    rd_short = rd[:15] if rd else 'N/A'
    print(f'{"OK" if ok else "ERR"} id={db_id} appid={appid} rd={rd_short} lang={lang[:20] if lang else "N/A"}')
