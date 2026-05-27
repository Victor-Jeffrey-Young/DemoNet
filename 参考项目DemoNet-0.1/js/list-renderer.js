var DemoNet = DemoNet || {};

(function (NS) {
    var PREFIX = '.';

    function renderIndexPage() {
        PREFIX = '.';
        DemoNet.renderNav({ activeKey: 'index', brandText: 'DemoNet', style: 'transparent', showSearch: true });
        DemoNet.renderFooter();

        DemoNet.loadJSON(PREFIX + '/data/carousel.json', function (carousel) {
            renderCarousel(carousel);
        });

        DemoNet.loadJSON(PREFIX + '/data/featured.json', function (featured) {
            renderFeatured(featured);
        });
    }

    function renderCarousel(items) {
        var indicators = '';
        var slides = '';
        items.forEach(function (item, i) {
            var active = i === 0 ? 'active' : '';
            indicators += '<li data-target="#carouselExampleIndicators" data-slide-to="' + i + '" class="' + active + '"></li>';
            slides += (
                '<div class="carousel-item ' + active + '" style="background-image: url(\'' + PREFIX + '/' + item.image + '\'); background-size: cover; background-position: center;">' +
                '<div class="carousel-caption">' +
                '<a href="' + DemoNet.detailLink(item.type, item.id) + '">' +
                '<h3>' + item.title + '</h3>' +
                '</a>' +
                '<p>' + item.desc + '</p>' +
                '</div>' +
                '</div>'
            );
        });

        var html = (
            '<div id="carouselExampleIndicators" class="carousel slide my-carousel my-carousel" data-ride="carousel">' +
            '<ol class="carousel-indicators">' + indicators + '</ol>' +
            '<div class="carousel-inner" role="listbox">' + slides + '</div>' +
            '<a class="carousel-control-prev" href="#carouselExampleIndicators" role="button" data-slide="prev">' +
            '<span class="carousel-control-prev-icon" aria-hidden="true"></span>' +
            '<span class="sr-only">上一页</span>' +
            '</a>' +
            '<a class="carousel-control-next" href="#carouselExampleIndicators" role="button" data-slide="next">' +
            '<span class="carousel-control-next-icon" aria-hidden="true"></span>' +
            '<span class="sr-only">下一页</span>' +
            '</a>' +
            '</div>'
        );

        var placeholder = document.getElementById('carousel-placeholder');
        if (placeholder) placeholder.innerHTML = html;
    }

    function renderFeatured(items) {
        var lis = '';
        items.forEach(function (item) {
            lis += (
                '<li class="scene">' +
                '<div class="movie" onclick="return true">' +
                '<div class="poster" style="background-image: url(\'' + PREFIX + '/' + item.posterImg + '\');"></div>' +
                '<div class="info">' +
                '<header style="background-image: url(\'' + PREFIX + '/' + item.headerImg + '\'); background-size: cover; background-position: center;">' +
                '<h1>' + item.title + '</h1>' +
                '<span class="year">' + item.year + '</span>' +
                '<span class="rating">' + item.rating + '</span>' +
                '<span class="duration">' + item.duration + '</span>' +
                '</header>' +
                '<p>' + item.description + '</p>' +
                '</div>' +
                '</div>' +
                '<br>' +
                '<a href="' + DemoNet.detailLink(item.type, item.id) + '">' +
                '<p class="bg-danger text-white text-center">' + item.title + '</p>' +
                '</a>' +
                '</li>'
            );
        });

        var html = (
            '<div class="wrapper">' +
            '<ul class="stage clearfix">' + lis + '</ul>' +
            '</div>'
        );

        var placeholder = document.getElementById('featured-placeholder');
        if (placeholder) placeholder.innerHTML = html;
    }

    function renderGamePage() {
        PREFIX = '.';
        DemoNet.loadJSON(PREFIX + '/data/games.json', function (games) {
            var swiperGames = games.filter(function (g) { return g.swiperOrder > 0; }).sort(function (a, b) { return a.swiperOrder - b.swiperOrder; });

            var slides = '';
            swiperGames.forEach(function (g) {
                slides += (
                    '<a class="swiper-slide" href="' + DemoNet.detailLink('game', g.id) + '" style="background-image: url(\'' + PREFIX + '/' + g.swiperCover + '\'); background-size: cover; background-position: center;"></a>'
                );
            });

            var swiperHtml = (
                '<div class="swiper-container gallery-top">' +
                '<div class="swiper-wrapper">' + slides + '</div>' +
                '<div class="wrap pr">' +
                '<div class="gallery-left pa">' +
                '<h2>热门推荐</h2>' +
                '<div class="swiper-pagination"></div>' +
                '</div>' +
                '</div>' +
                '<div class="swiper-button-next swiper-button-white"></div>' +
                '<div class="swiper-button-prev swiper-button-white"></div>' +
                '</div>'
            );

            var swiperPlaceholder = document.getElementById('swiper-placeholder');
            if (swiperPlaceholder) swiperPlaceholder.innerHTML = swiperHtml;

            var cards = '';
            games.forEach(function (g) {
                cards += (
                    '<div class="card" style="background-image: url(\'' + PREFIX + '/' + g.cardCover + '\'); background-size: cover; background-position: center;">' +
                    '<div class="content">' +
                    '<h2 class="title">' + g.cardTitle + '</h2>' +
                    '<p class="copy">' + g.cardDesc + '</p>' +
                    '<a href="' + DemoNet.detailLink('game', g.id) + '"><button class="btn">查看详情</button></a>' +
                    '</div>' +
                    '</div>'
                );
            });

            var cardsPlaceholder = document.getElementById('cards-placeholder');
            if (cardsPlaceholder) cardsPlaceholder.innerHTML = cards;

            DemoNet.renderNav({ activeKey: 'game', brandText: 'DemoNet 游戏频道', style: 'white', showSearch: true });
            DemoNet.renderFooter();

            if (typeof Swiper !== 'undefined') {
                var names = swiperGames.map(function (g) { return g.swiperName; });
                new Swiper('.gallery-top', {
                    pagination: '.swiper-pagination',
                    nextButton: '.swiper-button-next',
                    prevButton: '.swiper-button-prev',
                    paginationClickable: true,
                    autoplay: 5000,
                    paginationBulletRender: function (index, className) {
                        return '<span class="' + className + '">' + (names[index] || '') + '</span>';
                    }
                });
            }
        });
    }

    function renderMoviePage() {
        PREFIX = '.';
        DemoNet.loadJSON(PREFIX + '/data/movies.json', function (movies) {
            var sorted = movies.sort(function (a, b) { return a.slideOrder - b.slideOrder; });

            var slides = '';
            sorted.forEach(function (m, i) {
                var bgStyle = m.wideCover ? ' style="background-image: url(\'' + PREFIX + '/' + m.wideCover + '\')"' : '';
                slides += (
                    '<div class="slider__slide" data-slide="' + (i + 1) + '">' +
                    '<div class="slider__wrap">' +
                    '<div class="slider__back"' + bgStyle + '></div>' +
                    '</div>' +
                    '<div class="slider__inner"' + bgStyle + '>' +
                    '<div class="slider__content">' +
                    '<h1>' + m.title + '</h1>' +
                    '<h3>' + m.englishTitle + '</h3>' +
                    '<a class="go-to-next">next</a>' +
                    '<br>' +
                    '<div><a href="' + DemoNet.detailLink('movie', m.id) + '">详情</a></div>' +
                    '</div>' +
                    '</div>' +
                    '</div>'
                );
            });

            var sliderPlaceholder = document.getElementById('movie-slider-placeholder');
            if (sliderPlaceholder) sliderPlaceholder.innerHTML = slides + '<div class="slider__indicators"></div>';

            DemoNet.renderNav({ activeKey: 'movie', brandText: 'DemoNet电影频道', style: 'transparent', showSearch: true });
            DemoNet.renderFooter();

            var slideEls = $('.slider__slide');
            var totalSlides = slideEls.length;
            for (var i = 1; i <= totalSlides; i++) {
                $('.slider__indicators').append('<div class="slider__indicator" data-slide="' + i + '"></div>');
            }
            setTimeout(function () {
                $('.slider__wrap').addClass('slider__wrap--hacked');
            }, 1000);

            requestAnimationFrame(function () {
                requestAnimationFrame(function () {
                    $('.slider__slide').first().addClass('slider__slide--active');
                });
            });

            $(document).on('click', '.slider__next, .go-to-next', function () {
                var currentSlide = Number($('.slider__slide--active').data('slide'));
                currentSlide++;
                if (currentSlide > totalSlides) { currentSlide = 1; }
                $('.slider__slide').removeClass('slider__slide--active');
                $('.slider__slide[data-slide=' + currentSlide + ']').addClass('slider__slide--active');
            });
        });
    }

    function renderAnimePage() {
        PREFIX = '.';
        DemoNet.loadJSON(PREFIX + '/data/anime.json', function (animes) {
            var cards = '';
            animes.forEach(function (a) {
                cards += (
                    '<div class="col-md-4">' +
                    '<h4 class="text-center">' + a.displayTitle + '</h4>' +
                    '<hr>' +
                    '<div class="profile-card-2">' +
                    '<img src="' + PREFIX + '/' + a.cardImg + '" class="img img-responsive">' +
                    '<div class="profile-name">' +
                    '<a href="' + DemoNet.detailLink('anime', a.id) + '">' + a.displayTitle + '</a>' +
                    '</div>' +
                    '<div class="profile-username">' + a.tag + '</div>' +
                    '</div>' +
                    '</div>'
                );
            });

            var animeCardsPlaceholder = document.getElementById('anime-cards-placeholder');
            if (animeCardsPlaceholder) animeCardsPlaceholder.innerHTML = cards;

            DemoNet.renderNav({ activeKey: 'anime', brandText: 'DemoNet动漫频道', style: 'transparent', showSearch: true });
            DemoNet.renderFooter();
        });
    }

    function renderAboutPage() {
        PREFIX = '.';
        DemoNet.loadJSON(PREFIX + '/data/jobs.json', function (jobs) {
            var rows = '';
            jobs.forEach(function (j) {
                rows += (
                    '<tr>' +
                    '<th>' + j.position + '</th>' +
                    '<th>' + j.location + '</th>' +
                    '<th>' + j.type + '</th>' +
                    '<th>' + j.salary + '</th>' +
                    '</tr>'
                );
            });

            var jobsPlaceholder = document.getElementById('jobs-placeholder');
            if (jobsPlaceholder) jobsPlaceholder.innerHTML = rows;

            DemoNet.renderNav({ activeKey: 'about', brandText: 'DemoNet:关于我们', style: 'dark', showSearch: false });
            DemoNet.renderFooter();
        });
    }

    NS.renderIndexPage = renderIndexPage;
    NS.renderGamePage = renderGamePage;
    NS.renderMoviePage = renderMoviePage;
    NS.renderAnimePage = renderAnimePage;
    NS.renderAboutPage = renderAboutPage;
})(DemoNet);
