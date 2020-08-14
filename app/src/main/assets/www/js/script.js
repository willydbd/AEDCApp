/**
 * @author
 * Banjo Mofesola Paul | Oluwagbuyi Abimbola
 * Developers, Planet NEST
 * mofesolapaul@live.com | bimbowande@gmail.com
 */

// Global vars
//-----------------------------------------------
var VM;
var Me = this;
//Me.server = "http://192.168.43.138/aedcapp/";

// Init
//-----------------------------------------------
//------------Ajax timeout setting---------------
$.ajaxSetup({
	timeout: 10000
});
//--------------Viewmodel Setup------------------
$(function() {
	switch(app_where) {
	    case 'home':
	        VM = new HomeViewmodel();
	        break;
	    case 'menu':
	        VM = new MenuViewmodel();
	        break;
	    case 'topup':
	        VM = new TopupViewmodel();
	        break;
	    case 'usage-history':
	        VM = new UsageViewmodel();
	        break;
	}
	ko.applyBindings(VM);
});


// Home Viewmodel
//-----------------------------------------------
function HomeViewmodel() {
    var hvm = this;

    // observables
    hvm.company = ko.observable("Abuja Electricity Distribution Company");
    hvm.current_view = ko.observable("login");
    hvm.uname = ko.observable('');
    hvm.email = ko.observable('');
    hvm.pswd = ko.observable('');
    hvm.c_pswd = ko.observable('');
    hvm.show_content = ko.observable(false);

    // computed
    hvm.is_login_view = ko.computed(function() { return hvm.current_view() == 'login' });
    hvm.is_signup_view = ko.computed(function() { return hvm.current_view() == 'signup' });

    // event handlers
    hvm.goto_signup = function() {
        hvm.current_view('signup');
    };
    hvm.goto_login = function() {
        hvm.current_view('login');
    };
    hvm.submit_entry = function() {
        passed = false;
        if (hvm.is_signup_view()) {
            if (hvm.uname() == '') Android.swal('Enter your name','error');
            else if (hvm.pswd() != '' && (hvm.pswd() != hvm.c_pswd())) Android.swal('Passwords do not match','error');
            else passed = true;

            if (!passed) return;
        }

        passed = false;
        if (hvm.email() == '') Android.swal('Enter your email','error');
        else if (!checkEmail(hvm.email())) Android.swal('Sorry, the email looks incorrect','error');
        else if (hvm.pswd() == '') Android.swal('Enter your password','error');
        else passed = true;
        if (!passed) return;

        if (hvm.is_signup_view()) {
            if (Android.db().userExists(hvm.email())) Android.swal('Sorry, email is already registered','error');
            else {
                Android.db().signup(hvm.uname(), hvm.email(), hvm.pswd());
                window.location = "menu.htm";
            }
        } else {
            if (!Android.db().userExists(hvm.email())) Android.swal('Sorry, this email is not registered','error');
            else if (!Android.db().login(hvm.email(), hvm.pswd())) Android.swal('Incorrect password entered','error');
            else window.location = "menu.htm";
        }
    };

    // init
    if (isAndroidDevice() && Android.db().userLogged()) window.location = "menu.htm";
    else {
        p = Android.getInterpageData();
        if (p != "") {
            p = JSON.parse(p);
            if (p.msg_type == 'just-logged-out') {
                Android.clearNavHistory();
                hvm.email(p.last_user);
            }
        }
        hvm.show_content(true);
    }
}


// Menu Viewmodel
//-----------------------------------------------
function MenuViewmodel() {
    var mvm = this;

    // observables
    mvm.company = ko.observable("Abuja Electricity Distribution Company");
    mvm.logged_email = ko.observable();
    mvm.logged_uname = ko.observable();

    // event handlers
    mvm.show_forecast = function() {
        Android.db().showForecast();
    };
    mvm.logout = function() {
        Android.db().logout();
        Android.setInterpageData('{"msg_type": "just-logged-out", "last_user": "'+ mvm.logged_email() +'"}');
        window.location = "index.htm";
    };
    mvm.goto_topup = function() {
        window.location = "topup.htm";
    };
    mvm.goto_history = function() {
        window.location = "usage.htm";
    };

    // init
    Android.clearNavHistory();
    userdata = Android.db().getUserData();
    if (userdata != '') {
        userdata = JSON.parse(userdata);
        mvm.logged_email = ko.observable(userdata.email);
        mvm.logged_uname = ko.observable(userdata.uname);
    }
}


// Topup Viewmodel
//-----------------------------------------------
function TopupViewmodel() {
    var tvm = this;

    // observables
    tvm.company = ko.observable("Abuja Electricity Distribution Company");
    tvm.card_number = ko.observable('');
    tvm.cvv2 = ko.observable('');
    tvm.pin = ko.observable('');
    tvm.amt = ko.observable('');
    tvm.serial = ko.observable('');

    // computed
    tvm.kwh = ko.computed(function() {
        if (tvm.amt() == '') return '';
        else if (parseFloat(tvm.amt()) == 0) return '0.00';
        else {
            d = ((parseFloat(tvm.amt()) - (parseFloat(tvm.amt()) * 0.05)) / 0.8);
            return d.toFixed(2);
        }
    });
    tvm.cvv2_valid = ko.computed(function() {
        return !isNaN( tvm.cvv2() ) && !tvm.cvv2().toString().contains('.');
    });
    tvm.pin_valid = ko.computed(function() {
        return !isNaN( tvm.pin() ) && !tvm.pin().toString().contains('.');
    });
    tvm.amt_valid = ko.computed(function() {
        return !isNaN( tvm.amt() ) && !tvm.amt().toString().contains('.');
    });

    // event handlers
    tvm.topup_submit = function() {
        if (tvm.card_number() == '' || tvm.cvv2() == '' || tvm.pin() == '' || tvm.amt() == '' || tvm.serial() == '') Android.swal('Please enter all details','error');
        else if (tvm.card_number().length < 16) Android.swal('Debit card number must be 16 digits','error');
        else if (!tvm.cvv2_valid()) Android.swal('The CVV2 is improperly formatted, only numbers can be entered','error');
        else if (tvm.cvv2().length < 3) Android.swal('CVV2 must be 3 digits','error');
        else if (!tvm.pin_valid()) Android.swal('The PIN is improperly formatted, only numbers can be entered','error');
        else if (tvm.pin().length < 4) Android.swal('PIN must be 4 digits','error');
        else if (!tvm.amt_valid()) Android.swal('The amount is improperly formatted, only numbers can be entered','error');
        else if (parseInt(tvm.amt()) == 0) Android.swal('Amount cannot be zero','error');
        else if (tvm.serial().length < 20) Android.swal('Meter serial number must be 20 digits','error');
        else {
            payload = ko.toJSON(tvm);
            pin = Android.db().topup(payload);
            Android.swal(pin, "PIN CODE", '');
            tvm.cvv2('');
            tvm.pin('');
            tvm.amt('');
        }
    };

    // init
    userdata = Android.db().getUserData();
    if (userdata != '') {
        userdata = JSON.parse(userdata);
        tvm.card_number(userdata.card_num);
        tvm.serial(userdata.serial);
    }
}


// Usage Viewmodel
//-----------------------------------------------
function UsageViewmodel() {
    var uvm = this;

    // observables
    uvm.statistics   = ko.observableArray();
    uvm.first_shown  = false;

    // methods
    uvm.set_first_shown = function() {
        uvm.first_shown = true;
    };
    uvm.update_stats = function() {
        uvm.first_shown  = false;
        json = Android.db().get_statistics_payload();
        s = JSON.parse(json);
        uvm.statistics(s);
    };

    // init
    uvm.update_stats();
    userdata = Android.db().getUserData();
    if (userdata != '') {
        userdata = JSON.parse(userdata);
        uvm.logged_uname = ko.observable(userdata.uname);
    }
}


// Helper methods
//-----------------------------------------------
//--------------Is Android Device----------------
function isAndroidDevice() {
	return (typeof Android != "undefined");
}
//-----------------JS var_dump-------------------
function dump(obj) {
    var out = '';
    for (var i in obj) {
        out += i + ": " + obj[i] + "\n";
    }

    return out;
}
//------------Password reveal buttons------------
$(function() {
    $('.password + .input-group-addon').click(function() {
        $fa = $($(this).children()[0]);
        if ($fa.hasClass('fa-eye')) {
            $fa.removeClass('fa-eye');
            $fa.addClass('fa-eye-slash');
            $(this).prev('[type=text]').attr('type', 'password');
        } else {
            $fa.removeClass('fa-eye-slash');
            $fa.addClass('fa-eye');
            $(this).prev('[type=password]').attr('type', 'text');
        }
    });
});
//----------------Validate email-----------------
function checkEmail(emailAddress) {
  var reValidEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return reValidEmail.test(emailAddress);
}
//----------------String.contains----------------
if (typeof(String.prototype.contains) === 'undefined')
	String.prototype.contains = function(s) { return this.indexOf(s) != -1; }