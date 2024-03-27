package com.kgprojects;

public class ConnectionString
{
	public static String getSqlConnectionString(String user,String pass)
	{
		return String.format("Server=tcp:vkszndbserver.database.windows.net,1433;Initial Catalog=oms;Persist Security Info=False;User ID=%s;Password=%s;MultipleActiveResultSets=False;Encrypt=True;TrustServerCertificate=False;Connection Timeout=30;", user,pass);
	}
}