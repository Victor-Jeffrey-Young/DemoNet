var DemoNet = DemoNet || {};

(function (NS) {
    var NAV_ITEMS = [
        { label: '主页', href: 'index.html', key: 'index' },
        { label: '游戏', href: 'game.html', key: 'game' },
        { label: '电影', href: 'movie.html', key: 'movie' },
        { label: '动漫', href: 'anime.html', key: 'anime' },
        { label: '关于我们', href: 'about.html', key: 'about' }
    ];

    var STYLE_PRESETS = {
        dark: { bg: 'bg-dark', text: 'navbar-dark' },
        light: { bg: 'bg-light', text: 'navbar-light' },
        transparent: { bg: '', text: 'navbar-dark' },
        white: { bg: 'bg-white', text: 'navbar-light' }
    };

    function getBasePath() {
        var path = window.location.pathname;
        var depth = (path.match(/\//g) || []).length - 1;
        if (depth <= 1) return '.';
        var parts = [];
        for (var i = 0; i < depth - 1; i++) parts.push('..');
        return parts.join('/');
    }

    function renderNav(options) {
        var opts = options || {};
        var activeKey = opts.activeKey || 'index';
        var brandText = opts.brandText || 'DemoNet';
        var style = opts.style || 'dark';
        var showLogo = opts.showLogo !== false;
        var showSearch = opts.showSearch !== false;
        var fixed = opts.fixed !== false;

        var preset = STYLE_PRESETS[style] || STYLE_PRESETS.dark;
        var base = getBasePath();
        var logoSrc = base + '/img/logo.svg';

        var items = NAV_ITEMS.map(function (item) {
            var liCls = item.key === activeKey ? 'nav-item active' : 'nav-item';
            var href = base + '/' + item.href;
            var srOnly = item.key === activeKey ? '<span class="sr-only">(current)</span>' : '';
            return '<li class="' + liCls + '"><a class="nav-link" href="' + href + '">' + item.label + srOnly + '</a></li>';
        }).join('');

        var brandHtml = showLogo 
            ? '<a class="navbar-brand" href="' + base + '/index.html"><span style="font-weight: bold; font-size: 1.5rem; letter-spacing: 2px;">' + brandText + '</span></a>'
            : '<a class="navbar-brand" href="' + base + '/index.html">' + brandText + '</a>';

        var searchBtnClass = preset.text === 'navbar-light' ? 'btn-outline-dark' : 'btn-outline-light';

        var searchHtml = showSearch ? (
            '<form class="form-inline my-2 my-lg-0">' +
            '<input class="form-control mr-sm-2" type="search" placeholder="搜索游戏/电影/动漫..." aria-label="Search">' +
            '<button class="btn ' + searchBtnClass + ' my-2 my-sm-0" type="submit">搜索</button>' +
            '</form>'
        ) : '';

        var fixedCls = fixed ? 'sticky-top' : '';

        var nav = (
            '<header class="' + fixedCls + '">' +
            '<nav class="my-nav navbar ' + preset.text + ' ' + preset.bg + ' navbar-expand-lg" role="navigation" style="transition: all 0.3s ease-in-out;">' +
            '<div class="container">' +
            brandHtml +
            '<button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">' +
            '<span class="navbar-toggler-icon"></span>' +
            '</button>' +
            '<div class="collapse navbar-collapse justify-content-between" id="navbarNav">' +
            '<ul class="navbar-nav">' + items + '</ul>' +
            searchHtml +
            '</div>' +
            '</div>' +
            '</nav>' +
            '</header>'
        );

        var navPlaceholder = document.getElementById('nav-placeholder');
        if (navPlaceholder) {
            navPlaceholder.innerHTML = nav;
        }

        if (fixed) {
            setupScrollEffect(style);
        }
    }

    function setupScrollEffect(style) {
        var navbar = document.querySelector('.my-nav');
        if (!navbar) return;

        if (style === 'transparent') {
            window.addEventListener('scroll', function() {
                if (window.scrollY > 50) {
                    navbar.classList.add('bg-dark');
                } else {
                    navbar.classList.remove('bg-dark');
                }
            });
        }
    }

    function renderFooter() {
        var html = (
            '<footer id="sticky-footer" class="flex-shrink-0 py-4 bg-dark text-white-50">' +
            '<div class="container text-center">' +
            '<small>开发组：刘晓希，徐鑫 &nbsp;|&nbsp; &copy; ' + new Date().getFullYear() + ' DemoNet All Rights Reserved</small>' +
            '</div>' +
            '</footer>'
        );
        var footerPlaceholder = document.getElementById('footer-placeholder');
        if (footerPlaceholder) {
            footerPlaceholder.innerHTML = html;
        }
    }

    function loadJSON(url, callback) {
        var xhr = new XMLHttpRequest();
        xhr.overrideMimeType('application/json');
        xhr.open('GET', url, true);
        xhr.onreadystatechange = function () {
            if (xhr.readyState === 4 && xhr.status === 200) {
                callback(JSON.parse(xhr.responseText));
            }
        };
        xhr.send(null);
    }

    function getUrlParams() {
        var params = {};
        window.location.search.replace(/[?&]+([^=&]+)=([^&]*)/gi, function (m, key, val) {
            params[key] = decodeURIComponent(val);
        });
        return params;
    }

    function detailLink(type, id) {
        var base = getBasePath();
        return base + '/detail.html?type=' + type + '&id=' + id;
    }

    NS.renderNav = renderNav;
    NS.renderFooter = renderFooter;
    NS.loadJSON = loadJSON;
    NS.getUrlParams = getUrlParams;
    NS.detailLink = detailLink;
    NS.getBasePath = getBasePath;
    NS.NAV_ITEMS = NAV_ITEMS;
})(DemoNet);
