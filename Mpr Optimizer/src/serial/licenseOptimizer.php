<?php
$host = "localhost";
$user = "parkfind_user";
$password = "NzWFHyAOvVlL";
$database = "parkfind_licenses";
$connection = mysql_connect($host, $user, $password) or die("couldn't connect to server");
$db = mysql_select_db($database, $connection) or die(mysql_error());
$request_name = $_POST['name'];
$request_email =  $_POST['email'];
$request_mac =  $_POST['mac'];
$request_mode = $_POST['mode'];
$request_serial = $_POST['serial'];

if ($request_mode == 'NEW') {
	$serial = md5($request_mac);
	// 	$runquery = mysql_query($q);
	$to = $request_email;
	$subject = "Automatic Message: Serial Number";

	$headers = 'From: MPR Optimizer Team' . "\r\n";
	$body = "Dear " . $request_name . ",\n\nWe would like to welcome you to our family.\n\nPlease use the following key to activate your copy.\n\n\nE-mail: " . $request_email . "\nSerial Number: " . $serial . "\n\nBest Regards,\nMPR Optimizer Team.";
	if (mail($to, $subject, $body, $headers)) {
		$q = mysql_query("INSERT INTO licenses_optimizer (name, email, mac,serial) VALUES('$request_name','$request_email','$request_mac','$serial')") or die(mysql_error());
		echo 1;
	}
	else {
		echo 0;
	}
}
else if ($request_mode == 'VERIFY') {
	$q = mysql_query("SELECT * FROM licenses_optimizer WHERE mac='$request_mac'");
	if(is_resource($q) && mysql_num_rows($q) > 0 ){
		$q = mysql_fetch_assoc($q);
		if($request_serial == $q['serial']) {
			echo 1;
		} else {
			echo 0;
		}
	}
	else {
		echo 0;
	}
}

mysql_close();
?>