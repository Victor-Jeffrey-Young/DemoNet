(function () {
    var params = DemoNet.getUrlParams();
    var type = params.type;
    var id = params.id;

    if (!type || !id) {
        document.getElementById('detail-content').innerHTML = '<p>参数错误：缺少 type 或 id</p>';
        return;
    }

    var dataFiles = { game: 'data/games.json', movie: 'data/movies.json', anime: 'data/anime.json' };
    var dataFile = dataFiles[type];
    var basePath = DemoNet.getBasePath();
    var prefix = basePath + '/';

    DemoNet.loadJSON(prefix + dataFile, function (items) {
        var item = items.filter(function (i) { return i.id === id; })[0];
        if (!item) {
            document.getElementById('detail-content').innerHTML = '<p>未找到数据：' + id + '</p>';
            return;
        }

        var navStyle = type === 'game' ? 'white' : type === 'anime' ? 'light' : 'dark';
        document.title = 'DemoNet' + typeLabel(type) + '：' + item.title;
        DemoNet.renderNav({ activeKey: type, brandText: item.title, style: navStyle, showSearch: true });

        var html = '';

        html += '<br><br>';
        html += '<div class="row">';
        html += '<div class="col-md-4">';
        html += '<img src="' + prefix + item.cover + '" class="img-fluid" alt="' + item.title + '">';
        html += '</div>';
        html += '<div class="col-md-8">';
        html += '<h2>' + item.title + '</h2>';
        html += '<p>' + item.description + '</p>';
        html += '</div>';
        html += '</div>';

        html += '<hr>';
        html += '<h3>详细信息</h3>';
        html += '<dl class="row">';
        var info = item.info || {};
        Object.keys(info).forEach(function (key) {
            html += '<dt class="col-sm-3">' + key + '</dt>';
            html += '<dd class="col-sm-9">' + info[key] + '</dd>';
        });
        html += '</dl>';

        if (type === 'game' && item.about) {
            html += '<hr>';
            html += '<h3>关于这款游戏</h3>';
            html += '<p>' + item.about + '</p>';
        }

        if (type === 'game' && item.minSpec) {
            html += '<hr>';
            html += '<div class="row">';
            html += '<div class="col-md-6">';
            html += '<h4>最低配置</h4>';
            html += '<dl>';
            Object.keys(item.minSpec).forEach(function (key) {
                html += '<dt>' + key + '</dt>';
                html += '<dd>' + item.minSpec[key] + '</dd>';
            });
            html += '</dl>';
            html += '</div>';
            html += '<div class="col-md-6">';
            html += '<h4>推荐配置</h4>';
            html += '<dl>';
            Object.keys(item.recSpec || {}).forEach(function (key) {
                html += '<dt>' + key + '</dt>';
                html += '<dd>' + item.recSpec[key] + '</dd>';
            });
            html += '</dl>';
            html += '</div>';
            html += '</div>';
        }

        document.getElementById('detail-content').innerHTML = html;
        DemoNet.renderFooter();
    });

    function typeLabel(t) {
        if (t === 'game') return '游戏频道';
        if (t === 'movie') return '电影频道';
        if (t === 'anime') return '动漫频道';
        return '';
    }
})();
