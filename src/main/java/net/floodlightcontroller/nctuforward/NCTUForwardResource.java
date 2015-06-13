package net.floodlightcontroller.nctuforward;





import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;

import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;




public class NCTUForwardResource extends ServerResource {
    
    final static ObjectMapper om = new ObjectMapper();
    

    public String toJSON(Object obj) {
        StringWriter s = new StringWriter();
        try {
            om.writeValue(s, obj);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s.toString();
}
    
    @Get("json")
    public String retrieve() {
        INCTUForwardService fwd = (INCTUForwardService)getContext().getAttributes().get(INCTUForwardService.class.getCanonicalName());
        HashMap<String,Object> m = new HashMap<String,Object>(); 
        m.put("target_ip", fwd.getTargetIP());
        
        return toJSON(m);
    }
    
    @Post
    public void handlePost(String src) {
        System.out.printf("Src JSON:%s\n",src);
        
        INCTUForwardService fwd = (INCTUForwardService)getContext().getAttributes().get(INCTUForwardService.class.getCanonicalName());
        JsonParser jp;
        MappingJsonFactory f = new MappingJsonFactory();
        String key,value;
        try{
            jp = f.createJsonParser(src);
            jp.nextToken(); // SKIP {
            while(jp.nextToken() != JsonToken.END_OBJECT){
                key = jp.getCurrentName();
                jp.nextToken(); // SKIP :
                value = jp.getText();
                System.out.printf("%s =>%s\n",key,value);
                if(key == "target_ip"){
                    fwd.setTargetIP(value);
                    break;
                }
                
            }
            
        }
        catch (IOException e){
        }
        
    }
    
}
