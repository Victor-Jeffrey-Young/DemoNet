$(document).ready(function () {
	for (var i = 1; i <= $('.slider__slide').length; i++) {
		$('.slider__indicators').append('<div class="slider__indicator" data-slide="' + i + '"></div>')
	}
	setTimeout(function () {
		$('.slider__wrap').addClass('slider__wrap--hacked');
	}, 1000);
})

function goToSlide(number) {
	$('.slider__slide').removeClass('slider__slide--active');
	$('.slider__slide[data-slide=' + number + ']').addClass('slider__slide--active');
}

$('.slider__next, .go-to-next').on('click', function () {
	var currentSlide = Number($('.slider__slide--active').data('slide'));
	var totalSlides = $('.slider__slide').length;
	currentSlide++
	if (currentSlide > totalSlides) {
		currentSlide = 1;
	}
	goToSlide(currentSlide);
})

// document.addEventListener('DOMContentLoaded', function() {
// 	var slides = document.querySelectorAll('.slider__slide');
// 	var indicatorsContainer = document.querySelector('.slider__indicators');
// 	var nextBtn = document.querySelector('.slider__next');
// 	var currentSlide = 0;
  
// 	for (var i = 0; i < slides.length; i++) {
// 	  var slide = slides[i];
// 	  var indicator = document.createElement('div');
// 	  indicator.className = 'slider__indicator';
// 	  indicator.setAttribute('data-slide', i);
// 	  indicatorsContainer.appendChild(indicator);
// 	}
  
// 	setTimeout(function() {
// 	  var sliderWrap = document.querySelector('.slider__wrap');
// 	  sliderWrap.classList.add('slider__wrap--hacked');
// 	}, 1000);
  
// 	if (nextBtn) {
// 	  nextBtn.addEventListener('click', function() {
// 		currentSlide++;
// 		if (currentSlide >= slides.length) {
// 		  currentSlide = 0;
// 		}
// 		goToSlide(currentSlide);
// 	  });
// 	}
  
// 	function goToSlide(number) {
// 	  for (var i = 0; i < slides.length; i++) {
// 		slides[i].classList.remove('slider__slide--active');
// 	  }
// 	  slides[number].classList.add('slider__slide--active');
// 	}
//   });
  