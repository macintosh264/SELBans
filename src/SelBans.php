<?php
	/*
	 *	SELBans Web Interface Page
	 *		PHP, Java and SQL by Dominic Masters
	 *		CSS, HTML and Javascript by Jordan Atkins
	 *	
	 *	Configuration (Simple):
	 *		Change: new BanDatabase('localhost', '3306', 'minecraft', 'SelBans', 'root', 'password');
	 *		To suit your desires
	 *		
	 *		Format: new BanDatabase(host, port, database, prefix, username, password);
	 *		Match your config.yml settings. Make sure values are surrounded by ' quotes (like above).
	 */
	 
	function connectToSQL() {
		new BanDatabase('localhost', '3306', 'minecraft', 'SELBans', 'root', 'password');	//Change this line to suit your server.
	}
	 
	 
	/*
	 * Everything below is optional and only recommended for advanced users:
	 */
	
	
	$pageTitle = 'SELBans';
	$bootstrap = 'http://twitter.github.io/bootstrap/assets/js/bootstrap.js';
	$bootstrapCSS = 'http://twitter.github.io/bootstrap/assets/css/bootstrap.css';
	$jQuery = 'http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js';
	$minecraftUsernameRegexPHP = '/[^a-z0-9_]/i';
	$minecraftUsernameRegexJS = '/[^a-z0-9_]/gi';
	
	$pageName = basename($_SERVER['PHP_SELF']);
	
	/*
	 *		===	Ignore Everything Below ===
	 *	=======		   This Line		=======
	 */
	
	BanType::$ban = new BanType('ban');
	BanType::$kick = new BanType('kick');
	BanType::$warn = new BanType('warn');
	BanType::$mute = new BanType('mute');
	BanType::$demote = new BanType('demote');
	
	function closeSQL() {
		if(!isset(BanDatabase::$instance)) {
			return;
		}
		
		if(!BanDatabase::$instance->isConnected()) {
			return;
		}
		
		BanDatabase::$instance->closeSQL();
	}
	
	if(isset($_POST['request'])) {
		$request = strtolower($_POST['request']);
		checkRequest($request);
	}
	
	if(isset($_GET['request'])) {
		$request = strtolower($_GET['request']);
		checkRequest($request);
	}
	
	function checkRequest($req) {
		if($req == 'checksql') {
			connectToSQL();
			if(!BanDatabase::$instance->isConnected()) {
				sendRequest('false');
			} else {
				sendRequest('true');
			}
		}
		
		if($req == 'getplayer') {
			if(!isset($_POST['player'])) {
				sendRequest('NoPlayer');
			}
			
			$player = $_POST['player'];
			if(!isValidUsername($player)) {
				sendRequest("InvalidUsername");
			}
			
			connectToSQL();
			if(!BanDatabase::$instance->isConnected()) {
				sendRequest("SQLDown");
			}			
			sendRequest(BanDatabase::getPlayer($player));
		}
		
		if($req == 'searchplayer') {
			if(!isset($_POST['player'])) {
				sendRequest('NoPlayer');
			}
			
			$player = $_POST['player'];
			if(!isValidUsername($player)) {
				sendRequest("InvalidUsername");
			}
			
			connectToSQL();
			if(!BanDatabase::$instance->isConnected()) {
				sendRequest("SQLDown");
			}
			
			sendRequest(BanDatabase::searchPlayer($player));
		}
		
		die('invalid');
	}
	
	function sendRequest($data) {
		header('Content-Type: application/json');
		$jsonData = json_encode($data);
		echo $jsonData;
		closeSQL();
		die();
	}
	
	function isValidUsername($string) {
		global $minecraftUsernameRegexPHP;
		if(strlen($string) >= 20) {
			return 0;
		}
		
		return !preg_match($minecraftUsernameRegexPHP, $string);
	}
	
	class BanDatabase {
		public static $instance;
		
		private $host;
		private $port;
		private $db;
		private $table;
		private $prefix;
		private $username;
		private $password;
		
		private $connected = true;
		
		private $connection;
		
		public function __construct($host, $port, $database, $prefix, $username, $password, $table='Bans') {
			$this->host = $host;
			$this->port = $port;
			$this->db = $database;
			$this->table = $table;
			$this->prefix = $prefix;
			$this->username = $username;
			$this->password = $password;
			
			$this->connection = @mysql_connect($host . ':' . $port, $username, $password);
			if(!$this->connection) {
				$this->connected = false;
				BanDatabase::$instance = $this;
				return;
			}
			
			if(!mysql_select_db($database, $this->connection)) {
				$this->connected = false;
			}
			
			BanDatabase::$instance = $this;
		}
		
		public function isConnected() {
			return $this->connected;
		}
		
		public function closeSQL() {
			$this->connection = false;
			@mysql_close($connection);
		}
		
		public function getDBPrefix() {
			return '`' . $this->db . '`.`' . $this->prefix . $this->table . '` ';
		}
		
		public function getConnection() {
			return $this->connection;
		}
		
		public function fetchSQL($query) {
			$array = Array();
			$result = @mysql_query($query, $this->getConnection()) or mysql_error();
			while($data = @mysql_fetch_array($result)) {
				array_push($array, $data);
			}
			return $array;
		}
		
		public static function getPlayer($player) {
			$statement = '
				SELECT * FROM ' . BanDatabase::$instance->getDBPrefix() . '
				WHERE `player` LIKE \'' . $player . '\'
				ORDER BY `date` DESC;
			';
			
			$results = BanDatabase::$instance->fetchSQL($statement);
			$bans = Array();
			
			foreach($results as $result) {
				$isActive = false;
				
				if($result["active"] == "true") {
					$isActive = true;
				}
				
				if(BanType::getTypeFromString($result["type"]) == null) {
					continue;
				}
				
				$ban = new Ban(
					$result["playerby"],
					$result["player"],
					$result["reason"],
					BanLocation::createLocationFromString($result["pos"], ''),
					BanType::getTypeFromString($result["type"]),
					strtotime($result["date"]),
					strtotime($result["unbandate"]),
					$isActive,
					$result["id"]
				);
				
				array_push($bans, $ban);
			}
			
			return $bans;
		}
		
		public static function searchPlayer($player) {
			$statement = "
				SELECT
					`player` as 'Player',
					IF(ISNULL(bans.bans), 0, bans.bans) as 'Bans',
					IF(ISNULL(kicks.kicks), 0, kicks.kicks) as 'Kicks',
					IF(ISNULL(warns.warns), 0, warns.warns) as 'Warns',
					IF(ISNULL(demotes.demotes), 0, demotes.demotes) as 'Demotes',
					IF(ISNULL(mutes.mutes), 0, mutes.mutes) as 'Mutes'
				FROM
					" . BanDatabase::$instance->getDBPrefix() . "
					LEFT JOIN (
						SELECT
							`player` as 'p',
							COUNT(`type`) as 'bans'
						FROM
							" . BanDatabase::$instance->getDBPrefix() . "
						WHERE
							type LIKE 'ban'
						GROUP BY
							`player`
					) bans ON " . BanDatabase::$instance->getDBPrefix() . ".player = bans.p
					LEFT JOIN (
						SELECT
							`player` as 'p',
							COUNT(`type`) as 'kicks'
						FROM
							" . BanDatabase::$instance->getDBPrefix() . "
						WHERE
							type LIKE 'kick'
						GROUP BY
							`player`
					) kicks ON " . BanDatabase::$instance->getDBPrefix() . ".player = kicks.p
					LEFT JOIN (
						SELECT
							`player` as 'p',
							COUNT(`type`) as 'warns'
						FROM
							" . BanDatabase::$instance->getDBPrefix() . "
						WHERE
							type LIKE 'warn'
						GROUP BY
							`player`
					) warns ON " . BanDatabase::$instance->getDBPrefix() . ".player = warns.p
					LEFT JOIN (
						SELECT
							`player` as 'p',
							COUNT(`type`) as 'demotes'
						FROM
							" . BanDatabase::$instance->getDBPrefix() . "
						WHERE
							type LIKE 'demote'
						GROUP BY
							`player`
					) demotes ON " . BanDatabase::$instance->getDBPrefix() . ".player = demotes.p
					LEFT JOIN (
						SELECT
							`player` as 'p',
							COUNT(`type`) as 'mutes'
						FROM
							" . BanDatabase::$instance->getDBPrefix() . "
						WHERE
							type LIKE 'mute'
						GROUP BY
							`player`
					) mutes ON " . BanDatabase::$instance->getDBPrefix() . ".player = mutes.p
				WHERE
					`player` LIKE '" . str_replace('_', '\_', $player) . "%'
				GROUP BY
					`player`
				;
			";
			
			$results = BanDatabase::$instance->fetchSQL($statement);
			return $results;
		}
	}
	
	class BanLocation {
		public $x;
		public $y;
		public $z;
		public $world;
		
		public function __construct($x, $y, $z, $world) {
			$this->x = $x;
			$this->y = $y;
			$this->z = $z;
			
			if($world == "") {
				$world = "world";
			}
			
			$this->world = $world;
		}
		
		public function getX() {
			return $this->x;
		}
		
		public function getY() {
			return $this->y;
		}
		
		public function getZ() {
			return $this->z;
		}
		
		public function getWorld() {
			return $this->world;
		}
		
		public function toString() {
			return $this->getX() . ', ' . $this->getY() . ', ' . $this->getZ() . ' : ' . $this->getWorld();
		}
		
		public static function createLocationFromString($coords, $world) {
			$c = explode(', ', $coords);
			
			return new BanLocation($c[0], $c[1], $c[2], $world);
		}
	}
	
	class BanType {
		public static $ban;
		public static $kick;
		public static $warn;
		public static $demote;
		public static $mute;
		
		public $type;
		
		public function __construct($type) {
			$this->type = $type;
		}
		
		public function getType() {
			return $this->type;
		}
		
		public static function getTypeFromString($type) {
			$var = strtolower($type);
			if(!isset(BanType::$$var)) {
                            return null;
                        }
                        return BanType::$$var;
		}
	}
	
	class Ban {
		public $Banner = '';
		public $Banned = '';
		public $Reason = '';
		public $Location;
		public $Type;
		public $BannedDate;
		public $UnbanDate;
		public $IsActive;
		public $id;
		
		public function __construct($Banner, $BannedPlayer, $Reason, $Location, $Type, $BannedDate, $UnbanDate, $IsActive, $id) {
			$this->Banner = $Banner;
			$this->Banned = $BannedPlayer;
			$this->Location = $Location;
			$this->Type = $Type;
			$this->Reason = $Reason;
			$this->BannedDate = $BannedDate;
			$this->UnbanDate = $UnbanDate;
			$this->IsActive = $IsActive;
			$this->id = $id;
		}
		
		public function getBannedPlayer() {
			return $this->Banned;
		}
		
		public function getBanner() {
			return $this->Banner;
		}
		
		public function getReason() {
			return $this->Reason;
		}
		
		public function getLocation() {
			return $this->Location;
		}
		
		public function getType() {
			return $this->Type;
		}
		
		public function getBannedDate() {
			return $this->BannedDate;
		}
		
		public function isBanActive() {
			return $this->IsActive;
		}
		
		public function getUnbanDate() {
			return $this->UnbanDate;
		}
		
		public function getId() {
			return $this->id;
		}
	}
	/*** END PHP ***/
?>
<!DOCTYPE HTML>
<html lang="en">
	<head>
		<title><?php echo $pageTitle; ?></title>
		<meta charset="utf-8" />
		
		<!-- Import jQuery(c) From domsPlace -->
		<script type="text/javascript" src="<?php echo $jQuery; ?>"></script>
		<!-- Import bootstrap(c) From domsPlace -->
		<script type="text/javascript" src="<?php echo $bootstrap; ?>"></script>
		
		<!-- Global Functions -->
		<script type="text/javascript">
			$('body').off('.data-api');
			
			var minecraftUsernameRegex = <?php echo $minecraftUsernameRegexJS; ?>;
			var dbCon = false;
			var results = 0;
			var banCount = 0;
			
			//Checks to see if a string is a valid minecraft username
			function isValidUsername(username) {
				if(username.length >= 20) {
					return false;
				}
				
				if(username.length <= 1) {
					return false;
				}
				
				if(username.match(minecraftUsernameRegex)) {
					return false;
				}
				return true;
			}
			
			function getRequest(dta) {
				var returnData = null;
				
				$.ajax({
					type: "POST",
					dataType: "json",
					data: dta,
					async: false,
					success: function(data) {
						returnData = data;
					}
				});
				
				return returnData;
			}
			
			$(document).ready(function() {
				var result = getRequest({"request": "checkSQL"});
				if(!result) {
					dbCon = false;
				} else {
					dbCon = true;
				}
				
				docLoaded();
			});
			
			function docLoaded() {
				if(dbCon != true) {
					PermError('Error!', 'Failed to connect to the SQL Server.');
					return;
				}
				
				searchLetter('symbol');
			}
			
			function searchLetter(symbol) {
				clearAllData();
				if(symbol == "symbol") {
					for(var i = 0; i < 10; i++) {
						ts(i);
					}
					
					ts('_');
					return false;
				}
				
				ts(symbol);
				
				return false;
			}
			
			function trySearch() {
				if(!isValidUsername($("#usernameSearch").val())) {
					Error('Error!', 'Invalid username.');
					return false;
				}
				
				clearAllData();
				
				var username = $("#usernameSearch").val();
				
				ts(username);
				
				return false;
			}
			
			function ts(username) {
				if(!dbCon) {
					clearAllData();
					return;
				}
				var players = getRequest({"request": "searchPlayer", "player": username});
				
				for(var x = 0; x < players.length; x++) {
					var player = players[x];
					if(player == null) {
						continue;
					}
					formatPlayer(player);
				}
			}
			
			function formatPlayer(player) {
				addPlayer(player["Player"], player["Bans"], player["Kicks"], player["Warns"], player["Mutes"], player["Demotes"]);
			}
			
			function clearAllData() {
				results = 0;
				banCount = 0;
				$("#BansResults").html("");
				$("#ResultsFound").html("Searching...");
			}
			
			function Error(warning, message) {
				$("#Error").fadeIn();
				$("#Error").addClass("alert alert-error");
				$("#Error").html('<strong>' + warning + '</strong> ' + message);
				setTimeout(function() {
					$("#Error").fadeOut();
				}, 3000);
			}
			
			function PermError(warning, message) {
				$("#PermError").fadeIn();
				$("#PermError").addClass("alert alert-error");
				$("#PermError").html('<strong>' + warning + '</strong> ' + message);
			}
			
			function getFaceURL(player) {
				return 'http://achievecraft.com/tools/avatar/64/' + player + '.png';
			}
			
			function requestPlayers() {
				var returnData = getRequest({"request": "getPlayer", "player": player});
				
				if(returnData == null) {
					return;
				}
				
				for(var i = 0; i < returnData.length; i++) {
					var data = returnData[i];
					
					addBanType(
						data["Type"]["type"], 
						data["Banned"], 
						data["Banner"], 
						data["Reason"], 
						new Date(data["BannedDate"] * 1000),
						new Date(data["UnbanDate"] * 1000),
						data["IsActive"],
						data["id"],
						formatLocationSQL(data["Location"]), 
						formatWorldSQL(data["Location"])
					);
				}
			}
			
			function addPlayer(banned, bans, kicks, warns, mutes, demotes) {
				var el = $("#BansResults");
				
				var newBan = '' + 
					'<div class="well">' + 
					'<div class="PlayerImage"><a href="#" onClick="return getModalForPlayer(\'' + banned + '\');"><img src="' + getFaceURL(banned) + '" /></a></div>' +
					'<div class="PlayerInfo"><h3 style="margin-bottom: -4px; margin-right: 8px;"><a href="#" onClick="return getModalForPlayer(\'' + banned + '\');">' + banned + '</a></h3>' +
					'Bans: <span class="badge badge-important" style="margin-right: 8px;">' + bans + '</span> '+
					'Kicks: <span class="badge badge-warning" style="margin-right: 8px;">' + kicks + '</span><br />'+
					'Warnings: <span class="badge badge-info" style="margin-right: 8px;">' + warns + '</span> '+
					'Mutes: <span class="badge" style="margin-right: 8px;">' + mutes + '</span>'+
					'Demotions: <span class="badge" style="margin-right: 8px;">' + demotes + '</span>'+
					'</div>' +	
					'</div>';
				
				append(el, newBan);
				results ++;
			}
			
			function getSuffixFromDate(date) {
				var date = date.toString();
				var d = date.charAt(date.length-1);
				if(d == 1) {
					return 'st';
				}
				
				if(d == 2) {
					return 'nd';
				}
				
				if(d == 3) {
					return 'rd';
				}
				
				return 'th';
			}
			
			function getMonth(month) {
				var month = month + 1;
				switch(month) {
					case 1:
						return 'January';
					case 2:
						return 'February';
					case 3:
						return 'March';
					case 4:
						return 'April';
					case 5:
						return 'May';
					case 6:
						return 'June';
					case 7:
						return 'July';
					case 8:
						return 'August';
					case 9:
						return 'September';
					case 10:
						return 'October';
					case 11:
						return 'November';
					case 12:
						return 'Decemeber';
					default:
						return null;
				}
				return null;
			}
			
			function formatDate(date) {
				var day = date.getDate() + getSuffixFromDate(date.getDate());
				
				return day + ' of ' + getMonth(date.getMonth()) + ', ' + date.getFullYear() + ' at ' + formatHours(date);
			}
			
			function formatHours(date) {
				var hours = date.getHours();
				var minutes = date.getMinutes();
				var seconds = date.getSeconds();
				
				var AMPM = 'AM';
				
				if(hours > 12) {
					hours = hours - 12;
					AMPM = 'PM';
				}
				
				if(hours < 10) {
					hours = '0' + hours;
				}
				if(minutes < 10) {
					minutes = '0' + minutes;
				}
				if(seconds < 10) {
					seconds = '0' + seconds;
				}
				
				return hours + ':' + minutes + ':' + seconds + ' ' + AMPM;
			}
			
			function formatLocation(pos, world) {
				return pos;
			}
			
			function addBan(element, banned, banner, reason, when, untildate, isActive, id, pos, world, type, elID) {
				var until = '';
				
				if(formatDate(when) != formatDate(untildate)) {
					until = '<br />Until: ' + formatDate(untildate);
				}
				
				var active = '';
				if(isActive) {
					active = '<span class="label label-important" style="margin-left: 4px;">Active</span>';
				}
				
				var bModal = '' +
					'<div class="accordion-group">' +
					'<div class="accordion-heading">' + 
					'<a class="accordion-toggle" data-toggle="collapse" data-parent="#' + elID + ' #accordion" href="#collapse' + banCount + '">' +
					'ID #' + id + ' ' + banned + ' was ' + type + ' by ' + banner + active +
					'</a>' +
					'</div>' +
					'<div id="collapse' + banCount + '" class="accordion-body collapse">' +
					'<div class="accordion-inner"><small>' +
					'For: ' + reason + '<br />' +
					'When: ' + formatDate(when) +
					until +
					'<br />Where: ' + formatLocation(pos, world) +
					'</small></div>' +
					'</div>' +
					'</div>'
				;
				
				append(element, bModal);
				banCount++;
			}
			
			function append(element, data) {
				element.html(element.html() + data);
			}
			
			setInterval(function() {
				if(results <= 0) {
					$("#ResultsFound").html("No Results.");
					$("#BansResults").html('' +
						'<div class="alert alert-info">' +
						'<strong>No Results!</strong> Guess everyone\'s been good!'+
						'</div>'
					);
					return;
				}
				
				$("#ResultsFound").html(results + " Result");
				if(results != 1) {
					append($("#ResultsFound"), "s");
				}
			}, 500);
			
			function defaultTabs() {
				var def = '' +
				'<div class="accordion" id="accordion">' +
				'</div>';
				
				return def;
			}
			
			function addBanType(type, player, banner, reason, when, until, active, id, pos, world) {
				var element = $("#BansTabBans #accordion");
				var el = 'BansTabBans';
				var msg = 'banned';
				
				if(type == 'ban') {
					el = 'BansTabBans';
					msg = 'banned';
				}
				
				if(type == 'kick') {
					el = 'BansTabKicks';
					msg = 'kicked';
				}
				
				if(type == 'warn') {
					el = 'BansTabWarns';
					msg = 'warned';
				}
				
				if(type == 'mute') {
					el = 'BansTabMutes';
					msg = 'muted';
				}
				
				if(type == 'demote') {
					el = 'BansTabDemotes';
					msg = 'demoted';
				}
				
				element = $("#" + el + " #accordion");
				addBan(element, player, banner, reason, when, until, active, id, pos, world, msg, el);
			}
			
			function getPlayer(player) {
				var returnData = getRequest({"request": "getPlayer", "player": player});
				
				if(returnData == null) {
					return;
				}
				
				for(var i = 0; i < returnData.length; i++) {
					var data = returnData[i];
					
					addBanType(
						data["Type"]["type"], 
						data["Banned"], 
						data["Banner"], 
						data["Reason"], 
						new Date(data["BannedDate"] * 1000),
						new Date(data["UnbanDate"] * 1000),
						data["IsActive"],
						data["id"],
						formatLocationSQL(data["Location"]), 
						formatWorldSQL(data["Location"])
					);
				}
			}
			
			function formatLocationSQL(location) {
				return location["x"] + ", " + location["y"] + ", " + location["z"];
			}
			
			function formatWorldSQL(location) {
				return location["world"];
			}
			
			function checkForNothing(element) {
				if(element.html() != "") {
					return;
				}
				
				element.html('<div class="alert alert-info"><strong>No results.</strong></div>');
			}
			
			function getModalForPlayer(player) {
				banCount = 0;
				$("#BansModalLabel").html("Bans for " + player);
				
				$("#BansTabBans").html(defaultTabs());
				$("#BansTabKicks").html(defaultTabs());
				$("#BansTabWarns").html(defaultTabs());
				$("#BansTabMutes").html(defaultTabs());
				$("#BansTabDemotes").html(defaultTabs());
				
				getPlayer(player);
				
				checkForNothing($("#BansTabBans #accordion"));
				checkForNothing($("#BansTabKicks #accordion"));
				checkForNothing($("#BansTabWarns #accordion"));
				checkForNothing($("#BansTabMutes #accordion"));
				checkForNothing($("#BansTabDemotes #accordion"));
				
				$("#BansModal").modal('show');
				
				return false;
			}
		</script>
		
		<link href="<?php echo $bootstrapCSS; ?>" rel="stylesheet" media="screen">
		
		<style type="text/css">
			.PlayerImage {
				float: left;
				clear: both;
				margin-right: 12px;
				height: 70px;
			}
			
			.PlayerInfo {
				height: 70px;
				margin-top: -20px;
			}
			
			.PlayerInfoButton {
				float: right;
				clear: none;
			}
			
			.PlayerImage img {
				border-radius: 4px;
				min-width: 64px;
				min-height: 64px;
				display: inline-block;
				background: #DDD;
			}
		</style>
	</head>
	
	<body>
		<div id="PageTop"></div>
		
		<div id="BansModal" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="BansModal" aria-hidden="true">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
				<h3 id="BansModalLabel">Bans for Player</h3>
			</div>
			
			<div class="modal-body" id="BansModalBody">
				<div class="tabbable tabs-left">
					<ul class="nav nav-tabs">
						<li class="active"><a href="#BansTabBans" data-toggle="tab">Bans</a></li>
						<li><a href="#BansTabKicks" data-toggle="tab">Kicks</a></li>
						<li><a href="#BansTabWarns" data-toggle="tab">Warnings</a></li>
						<li><a href="#BansTabMutes" data-toggle="tab">Mutes</a></li>
						<li><a href="#BansTabDemotes" data-toggle="tab">Demotions</a></li>
					</ul>
					<div class="tab-content">
						<div class="tab-pane active" id="BansTabBans">
						</div>
						<div class="tab-pane" id="BansTabKicks">
						</div>
						<div class="tab-pane" id="BansTabWarns">
						</div>
						<div class="tab-pane" id="BansTabMutes">
						</div>
						<div class="tab-pane" id="BansTabDemotes">
						</div>
					</div>
				</div>
			</div>
			
			<div class="modal-footer">
				<button class="btn" data-dismiss="modal" aria-hidden="true">Ok</button>
			</div>
		</div>
		
		<div class="container">
			<div class="navbar">
				<div class="navbar-inner">
					<div class="brand"><a href="#" onClick="return searchLetter('symbol');">SELBans Web Interface</a></div>	
					<form class="navbar-form pull-right" onSubmit="return trySearch();">
						<input type="text" id="usernameSearch" placeholder="Enter a username..." />
						<button type="submit" class="btn btn-primary">Search</button>
					</form>
				</div>
			</div>
			
			<div id="Error" style="display: none;">
				
			</div>
			
			<div id="PermError" style="display: none;">
				
			</div>
			
			<div id="ResultsFound" style="margin-left: 8px;">Searching...</div>
			
			<div class="pagination" style="margin-left: auto; margin-right: auto; display: block; width: 100%; text-align: center;">
				<ul>
					<li><a href="#" onClick="return searchLetter('symbol');">0-9</a></li>
					<li><a href="#" onClick="return searchLetter('A');">A</a></li>
					<li><a href="#" onClick="return searchLetter('B');">B</a></li>
					<li><a href="#" onClick="return searchLetter('C');">C</a></li>
					<li><a href="#" onClick="return searchLetter('D');">D</a></li>
					<li><a href="#" onClick="return searchLetter('E');">E</a></li>
					<li><a href="#" onClick="return searchLetter('F');">F</a></li>
					<li><a href="#" onClick="return searchLetter('G');">G</a></li>
					<li><a href="#" onClick="return searchLetter('H');">H</a></li>
					<li><a href="#" onClick="return searchLetter('I');">I</a></li>
					<li><a href="#" onClick="return searchLetter('J');">J</a></li>
					<li><a href="#" onClick="return searchLetter('K');">K</a></li>
					<li><a href="#" onClick="return searchLetter('L');">L</a></li>
					<li><a href="#" onClick="return searchLetter('M');">M</a></li>
					<li><a href="#" onClick="return searchLetter('N');">N</a></li>
					<li><a href="#" onClick="return searchLetter('O');">O</a></li>
					<li><a href="#" onClick="return searchLetter('P');">P</a></li>
					<li><a href="#" onClick="return searchLetter('Q');">Q</a></li>
					<li><a href="#" onClick="return searchLetter('R');">R</a></li>
					<li><a href="#" onClick="return searchLetter('S');">S</a></li>
					<li><a href="#" onClick="return searchLetter('T');">T</a></li>
					<li><a href="#" onClick="return searchLetter('U');">U</a></li>
					<li><a href="#" onClick="return searchLetter('V');">V</a></li>
					<li><a href="#" onClick="return searchLetter('W');">W</a></li>
					<li><a href="#" onClick="return searchLetter('X');">X</a></li>
					<li><a href="#" onClick="return searchLetter('Y');">Y</a></li>
					<li><a href="#" onClick="return searchLetter('Z');">Z</a></li>
				</ul>
			</div>
			
			<hr />
			
			<div id="BansResults">
			</div>
			
			<hr />
			<footer>
				<div class="pull-right" style="float: right; clear: both;"><a href="#PageTop">Back to Top</a></div>
				<a href="http://dev.bukkit.org/bukkit-plugins/selbans/">SELBans</a> Web Interface.<br />
				Code by <a href="http://domsplace.com/">Dominic Masters</a> and <a href="http://oxafemble.me">Jordan Atkins</a>. This version is specifically for Nicholas Cage!
			</footer>
		</div>
	</body>
</html>