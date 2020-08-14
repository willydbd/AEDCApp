$(function()
{
	$("input#createAccount").click(function(){
		$(".for-login").hide()
		$(".for-signup").show()
	});

	$("input#loginMenuBtn").click(function(){
		$(".for-signup").hide()
		$(".for-login").show()
	});

})