# See: http://www.appveyor.com/docs/services-databases#sql2012
# Set the $instanceName value below to the name of the instance you
# want to configure a static port for. This could conceivably be
# passed into the script as a parameter.

[reflection.assembly]::LoadWithPartialName("Microsoft.SqlServer.Smo") | Out-Null
[reflection.assembly]::LoadWithPartialName("Microsoft.SqlServer.SqlWmiManagement") | Out-Null

$serverName = $env:COMPUTERNAME
$instanceName = $env:INSTANCENAME
$wmi = new-object ('Microsoft.SqlServer.Management.Smo.Wmi.ManagedComputer')

echo "computer name: $serverName"
echo "instance name: $instanceName"

# For the named instance, on the current computer, for the TCP protocol,
# loop through all the IPs and configure them to use the standard port of 1433.
$urn = "ManagedComputer[@Name='$serverName']/ServerInstance[@Name='$instanceName']/ServerProtocol[@Name='Tcp']"

echo "getting uri: $urn"

$Tcp = $wmi.GetSmoObject($urn)
foreach ($ipAddress in $Tcp.IPAddresses)
{
    $ipAddress.IPAddressProperties["TcpDynamicPorts"].Value = ""
    $ipAddress.IPAddressProperties["TcpPort"].Value = "1433"
}
$Tcp.Alter()

# Restart the named instance of SQL Server to enable the changes.
Restart-Service "MSSQL`$$instanceName"
