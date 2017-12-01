<?php
    // PHP Data Objects(PDO) Sample Code:
    try {
        $conn = new PDO("sqlsrv:server = tcp:mjtakenon.database.windows.net,1433; Database = journeys", "mjtakenon", "int*P=5;");
        $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    }
    catch (PDOException $e) {
        print("Error connecting to SQL Server.");
        die(print_r($e));
    }

    //POSTからJSONに分解
    //echo $_POST[""].'<br>';
    //$json = $_POST[""];

    $json = file_get_contents('php://input'); //postされたものはこれでとれるらしい
    
    if($json == null) {
        echo "jsonが入力されませんでした<br>";
        
        $sql = "select count(JID) from Journey;";
        
        echo $sql."<br>";

        $num = $conn->query($sql);
        echo $num."<br>";
        $sql  = "insert into Journey values(".$num.",'".$jsonarray["name"]."');";
    
        echo $sql."<br>";
        return;
    }
    
    $jsonarray = json_encode($data);

    $sql = "select count(JID) from Journey;";

    $num = $conn->query($sql);

    $sql  = "insert into Journey values(".$num.",'".$jsonarray["name"]."');";

    $conn->query($sql);
    

    //$obj = json_decode($json_string);
    //var_dump($obj);

    /*$sql = "select * from journey;";
    $ret = $conn->query($sql);
    foreach($ret as $row) {
        echo $row['JID'].':'.$row['JName'].'<br>';
    }*/

?>