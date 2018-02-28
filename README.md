# https-multipart-purejava
A java client which implements multipart form submit over https and authentication implemented with only jdk classes and no external dependencies.


```sh
	private static String body = "{\"key1\":\"val1\", \"key2\":\"val2\"}";
	private static String subdata1 = "@@ -2,3 +2,4 @@\r\n";
	private static String subdata2 = "<data>subdata2</data>";
	
	public static void main(String[] args) throws Exception{		
		String url = "https://" + ip + ":" + port + "/dataupload";
		String token = "Basic "+ Base64.getEncoder().encodeToString((userName+":"+password).getBytes());
		
		MultipartBuilder multipart = new MultipartBuilder(url,token);		
        multipart.addFormField("entity", "main", "application/json",body);
        multipart.addFormField("attachment", "subdata1", "application/octet-stream",subdata1);
        multipart.addFormField("attachment", "subdata2", "application/octet-stream",subdata2);        
        List<String> response = multipart.finish();         
        for (String line : response) {
            System.out.println(line);
        }
	}
```
