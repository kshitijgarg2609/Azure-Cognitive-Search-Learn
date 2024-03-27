package com.kgprojects;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Properties;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.AzureCliCredential;
import com.azure.identity.AzureCliCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.search.models.SearchService;
import com.azure.resourcemanager.search.models.SkuName;
import com.azure.search.DataSources;
import com.azure.search.SearchApiKeyCredential;
import com.azure.search.SearchServiceClient;
import com.azure.search.SearchServiceClientBuilder;
import com.azure.search.models.DataType;
import com.azure.search.models.Field;
import com.azure.search.models.Index;
import com.azure.search.models.Indexer;

public class SearchServicesBuildAndConfigure
{
	public static void main(String[] args)throws Exception
	{
		System.out.println("START");
		
		
		Properties props = new Properties();
		FileInputStream fis = new FileInputStream("input.properties");
		props.load(fis);
		fis.close();
		
//		ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
//		        .clientId("")
//		        .clientSecret("")
//		        .tenantId("")
//		        .build();
		
		AzureCliCredential cli = new AzureCliCredentialBuilder().build();
		AzureResourceManager arm = AzureResourceManager
				.authenticate(cli,new AzureProfile(AzureEnvironment.AZURE))
				.withDefaultSubscription();
		
		
		SearchService ss = arm
				.searchServices()
				.define(props.getProperty("serviceName"))
				.withRegion(Region.fromName(props.getProperty("location")))
				.withExistingResourceGroup(props.getProperty("resourceGroupName"))
				.withSku(SkuName.fromString(props.getProperty("sku")))
				.create();
		
		
		SearchServiceClient ssc = new SearchServiceClientBuilder()
				.endpoint(String.format("https://%s.search.windows.net", ss.name()))
				.credential(new SearchApiKeyCredential(ss.getAdminKeys().primaryKey()))
				.buildClient();
		
		
		ssc.createDataSource(DataSources.createFromAzureSql("categories-datasource"
				, ConnectionString.getSqlConnectionString(props
						.getProperty("DBUser"),props.getProperty("DBPass")), "[categories]"));
		ssc.createDataSource(DataSources.createFromAzureSql("orders-datasource"
				, ConnectionString.getSqlConnectionString(props
						.getProperty("DBUser"),props.getProperty("DBPass")), "[vw_OrderDetails]"));
		ssc.createDataSource(DataSources.createFromAzureSql("products-datasource"
				, ConnectionString.getSqlConnectionString(props
						.getProperty("DBUser"),props.getProperty("DBPass")), "[products]"));

		
		ssc.createIndex(new Index()
				.setName("categories-index")
				.setFields(Arrays.asList(
				new Field().setName("category_id").setType(DataType.EDM_STRING).setKey(true),
				new Field().setName("category_name").setType(DataType.EDM_STRING)
				)));
		
		ssc.createIndex(new Index()
				.setName("orders-index")
				.setFields(Arrays.asList(
				new Field().setName("order_date").setType(DataType.EDM_STRING),
				new Field().setName("order_number").setType(DataType.EDM_STRING).setKey(true),
				new Field().setName("customer_name").setType(DataType.EDM_STRING),
				new Field().setName("product_name").setType(DataType.EDM_STRING),
				new Field().setName("price").setType(DataType.EDM_STRING),
				new Field().setName("description").setType(DataType.EDM_STRING),
				new Field().setName("image_url").setType(DataType.EDM_STRING),
				new Field().setName("date_added").setType(DataType.EDM_DATE_TIME_OFFSET),
				new Field().setName("category_name").setType(DataType.EDM_STRING)
				)));
		
		ssc.createIndex(new Index()
				.setName("products-index")
				.setFields(Arrays.asList(
				new Field().setName("product_id").setType(DataType.EDM_STRING).setKey(true),
				new Field().setName("product_name").setType(DataType.EDM_STRING),
				new Field().setName("category_id").setType(DataType.EDM_INT32),
				new Field().setName("price").setType(DataType.EDM_STRING),
				new Field().setName("description").setType(DataType.EDM_STRING),
				new Field().setName("date_added").setType(DataType.EDM_STRING),
				new Field().setName("image_url").setType(DataType.EDM_DATE_TIME_OFFSET)
				)));
		
		ssc.createIndexer(new Indexer()
				.setName("categories-indexer")
				.setDataSourceName("categories-datasource")
				.setTargetIndexName("categories-index"));
		
		ssc.createIndexer(new Indexer()
				.setName("orders-indexer")
				.setDataSourceName("orders-datasource")
				.setTargetIndexName("orders-index"));
		
		ssc.createIndexer(new Indexer()
				.setName("products-indexer")
				.setDataSourceName("products-datasource")
				.setTargetIndexName("products-index"));
		
		System.out.println("STOP");
	}
}