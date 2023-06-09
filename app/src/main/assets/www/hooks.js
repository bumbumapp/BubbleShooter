
var gradle = {
    debug : true,
	isMobile : ( /(ipad|iphone|ipod|android|windows phone)/i.test(navigator.userAgent) ),

	//Ads information :
	//===================
    banner         : 'ca-app-pub-8444865753152507/4808738852', //id placement banner
    interstitial   : '', //id placement interstitial

    isTesting      : false, //Ads mode testing. set to false for a production mode.
    enableBanner   : true, //Ads enable the banner. set to false to disable the banner.
    bannerAtBottom : true, //if false the banner will be at top
    overlap        : false,


	intervalAds    : 1,     //Ads each interval for example each 3 times
	
	

    

	//Events manager :
	//================
    event: function(ev, msg){
        gradle.log(ev,msg);
        switch(ev){
            case 'first_start':   //First start
                //gradle.showInter();
                break;
			case 'menu': //menu
                //gradle.showInter();
                break;
			case 'play': //play
                gradle.checkInterval() && gradle.showInter();
                break;
			case 'game_over':
                gradle.showInter();
                break;
			case 'level_complete':
                //gradle.showInter();
                break;
			case 'game_pause':
                gradle.showInter();
                break;
			case 'game_over':
                //gradle.showInter();
                break;
			case 'more_games': // <-- End of game
                gradle.more();
                break;
			case 'test':
				//gradle.checkInterval() && gradle.showInter();
                break;			
        }
    },
	
	map_position: "s,105,10872@s,186,10821@s,267,10777@s,313,10708@s,315,10625@s,267,10544@s,256,10463@s,234,10385@s,120,10359@s,113,10294@s,160,10228@s,119,10153@s,79,10071@s,159,10017@s,253,9981@s,212,9915@s,123,9892@s,89,9809@s,166,9756@s,233,9703@s,263,9622@s,224,9553@s,172,9503@s,221,9427@s,140,9377@s,112,9300@s,129,9223@s,212,9187@s,282,9079@s,184,9026@s,83,8989@s,136,8908@s,245,8904@s,309,8835@s,195,8789@s,59,8758@s,142,8661@s,252,8582@s,300,8486@s,234,8396@s,323,8284@s,210,8192@s,91,8154@s,140,8087@s,220,8032@s,300,7967@s,316,7886@s,275,7790@s,253,7669@s,104,7571@s,153,7456@s,109,7364@s,109,7275@s,213,7234@s,272,7178@s,144,7148@s,94,7049@s,170,6983@s,249,6935@s,252,6821@s,166,6745@s,212,6642@s,112,6556@s,113,6480@s,191,6436@s,242,6372@s,282,6312@s,194,6259@s,83,6235@s,122,6153@s,207,6143@s,283,6135@s,316,6070@s,233,6027@s,139,6028@s,55,5972@s,127,5891@s,219,5834@s,304,5736@s,237,5649@s,263,5559@s,310,5465@s,195,5423@s,110,5350@s,210,5275@s,300,5190@s,316,5089@s,245,4979@s,243,4862@s,104,4784@s,153,4699@s,173,4619@s,89,4557@s,139,4483@s,233,4467@s,262,4372@s,164,4362@s,90,4310@s,99,4228@s,198,4192@s,256,4129@s,256,4048@s,197,3984@s,230,3919@s,172,3869@s,113,3803@s,119,3719@s,212,3646@s,272,3595@s,254,3525@s,163,3486@s,76,3426@s,185,3360@s,299,3371@s,295,3285@s,207,3245@s,102,3257@s,74,3169@s,187,3088@s,290,3022@s,256,2910@s,250,2808@s,331,2720@s,220,2665@s,90,2618@s,150,2543@s,236,2492@s,305,2406@s,303,2315@s,250,2213@s,254,2107@s,153,2062@s,109,1980@s,169,1886@s,83,1800@s,152,1724@s,274,1665@s,159,1613@s,79,1531@s,167,1435@s,246,1372@s,256,1291@s,187,1227@s,220,1142@s,152,1072@s,113,986@s,169,922@s,232,849@s,282,768@s,204,728@s,93,701@s,106,624@s,205,603@s,309,584@s,275,498@s,157,487@s,52,450@s,124,362@s,217,305@s,300,205",
	
	

    log: function(val,msg){
        val && gradle.debug && console.log( gradle.isMobile && (typeof val === 'object' && val !== null) ? JSON.stringify(val) : val );
        msg && gradle.debug && console.log( gradle.isMobile && (typeof msg === 'object' && msg !== null) ? JSON.stringify(msg) : msg );
    },
	
	
	//Ready : /!\ DO NOT CHANGE, ONLY IF YOU ARE AN EXPERT.
	//=========================
    ready: function() {
        console.log('gradle ready ...');
        if(typeof admob !='undefined'){
            if(gradle.isTesting){
                admob.banner.config({
                    id: gradle.banner,
                    isTesting: true,
                    autoShow: true,
                    overlap: gradle.overlap,
                    offsetTopBar: false,
                    bannerAtTop: !gradle.bannerAtBottom
                });

                admob.interstitial.config({
                    id: gradle.interstitial,
                    isTesting: true,
                    autoShow: false,
                });
            }
            else{
                admob.banner.config({
                    id: gradle.banner,
                    autoShow: true,
                    overlap: gradle.overlap,
                    offsetTopBar: false,
                    bannerAtTop: !gradle.bannerAtBottom
                });

                admob.interstitial.config({
                    id: gradle.interstitial,
                    autoShow: false,
                });
            }
        }
        if(gradle.enableBanner && typeof admob!=='undefined'){
            admob.banner.prepare();
        }
        gradle.prepareInter();
         document.addEventListener('admob.banner.events.LOAD_FAIL', function(event) {
           gradle.log(event);
        });

        document.addEventListener('admob.banner.events.LOAD', function(event) {
           gradle.log(event);
        });

        document.addEventListener('admob.interstitial.events.LOAD_FAIL', function(event) {
           gradle.log(event);
        });

        document.addEventListener('admob.interstitial.events.LOAD', function(event) {
           gradle.log(event);
        });

        document.addEventListener('admob.interstitial.events.CLOSE', function(event) {
           gradle.log(event);
           admob.interstitial.prepare();
        });

		//document.addEventListener("backbutton", function() {}, !1); - 1 < navigator.userAgent.indexOf("Windows Phone") && (SOUNDS_ENABLED = !1);
        document.addEventListener("visibilitychange", gradle.onVisibilityChanged, false);
		document.addEventListener("mozvisibilitychange", gradle.onVisibilityChanged, false);
		document.addEventListener("webkitvisibilitychange", gradle.onVisibilityChanged, false);
		document.addEventListener("msvisibilitychange", gradle.onVisibilityChanged, false);
		gradle.event('first_start');
		setTimeout(function(){
			gradle.start();
		},1000);
		
		gradle.hideSplash();
    },
	
	start: function(){
		setTimeout(function(){
			gameContainer = document.getElementById('gameContainer');
            gamePlay = new GamePlay();
            GameLib.init("Bubble", 480, 800);
		},10);
	},
	
	orderInit: function(a) {
		switch (a) {
			case 1:
				gradle.event('loading');
				loadRec.creatloading();
				break;
			case 2:
				gradle.event('menu');
				GameMenu.showMenu();
				break;
			case 3:
				gradle.event('help');
				GameHelp.showHelp();
				break;
			case 4:
				gradle.event('play');
				gamePlay.showPlay();
				break;
			case 5:
				//gradle.event('game_over');
				GameOver.showOver();
				break;
		}
	},

    more: function(){
        (gradle.developer_link!=="")&&window.open(gradle.developer_link);
    },

    hideSplash: function(){
        if(gradle.isMobile){
            setTimeout(function(){
                //cordova.exec(null, null, "SplashScreen", "hide", []);
            },1200);
        }
    },

    prepareInter: function(){
        if(!gradle.isMobile || typeof admob=='undefined' || admob==null) return;
        admob.interstitial.prepare();
    },

    showInter: function(){
        if(!gradle.isMobile || typeof admob=='undefined' || admob==null) return;
        admob.interstitial.show();
    },

    run : function(){
        gradle.log('gradle run ...');
        //gradle.ready();
		gradle.isMobile ? document.addEventListener('deviceready', gradle.ready, false) :  gradle.ready();
    },
	
	onVisibilityChanged : function(){
	    console.log('onVisibilityChanged...');
		try{
			if (document.hidden || document.mozHidden || document.webkitHidden || document.msHidden){
				//gradle_onPauseRequested();
			}else{
				//gradle_onResumeRequested();
			}
		}catch(error){}
	},
	
	currentInterval : 0,
	checkInterval: function(){
		return (++gradle.currentInterval==gradle.intervalAds) ? !(gradle.currentInterval=0) : !1;
	},
	
	buildKey : function(key){
        return "gd.4006."+key;
    },

    getStorage: function(key, default_value){
        var value;
        try {
            value = localStorage.getItem(gradle.buildKey(key));
        }
        catch(error){
			return default_value;
        }
		if(value !== undefined && value !=null){
            value = window.atob(value);
        }
		else{
			value = default_value;
		}
        return value;
    },

    setStorage: function(key, value){
        var v = value;
        if(v !== undefined){
            v = window.btoa(v);
        }
        try{
            localStorage.setItem(gradle.buildKey(key), v);
            return value;
        }
        catch(error){
            return undefined;
        }
    }
};

gradle.run();

var ThirdParty = {

    centerHorizontally: true,
    centerVertically: true,
    showRotateHint: true,
    enableFullscreenToggle: true,

    // Called when all assets have been loaded
    loadingComplete: function() {
        //console.log('API: loadingComplete');
    },

    // Called when the main menu is showed
    mainMenu: function() {
        //console.log('API: mainMenu');
    },

    gameHelp: function() {
        //console.log('API: gameHelp');
        //GameAnalytics.hit('GameHelp');
    },

    // Called when the game (first level) starts
    gameStart: function() {
        //console.log('API: gameStart');
        gradle.event('game_start');
    },

    // Called when a level is completed
    levelComplete: function(level) {
        //console.log('API: levelComplete: ' + level);
        //GameAnalytics.hit('LevelComplete', level);
         gradle.event('level_complete');
    },

    // Called when game over
    gameOver: function() {
        //console.log('API: gameOver');
        //GameAnalytics.hit('GameOver');
         gradle.event('game_over');
    },

    // Called when game is restarted
    restartGame: function() {
        //console.log('API: restartGame');
         gradle.event('game_restart');
    },

    // Called when game is completed (all levels completed)
    gameComplete: function() {
        //console.log('API: gameComplete');
        //GameAnalytics.hit('GameComplete');
    },

    showLeaderboard: function() {
		gradle.event('more_games');
        //console.log('API: showLeaderboard');
        //GameHiscore.leaderBoard();

    },

    // Called when user is ready to submit a score.
    submitScore: function(score, level) {

    }
};



