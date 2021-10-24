package nak.mci.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

@Service
public class WebPageContentFetcherURL implements WebPageContentFetcher{



    @Override
    public String fetchContent(String inputUrl) throws IOException {

        //Instantiating the URL class
        URL url = new URL(inputUrl);
        //Retrieving the contents of the specified page
        Scanner sc = new Scanner(url.openStream());
        //Instantiating the StringBuffer class to hold the result
        StringBuffer sb = new StringBuffer();
        while(sc.hasNext()) {
            sb.append(sc.next());
        }
        sc.close();
        //Retrieving the String from the String Buffer object
        String result = sb.toString();
        System.out.println(result);
        //Removing the HTML tags
        result = result.replaceAll("<[^>]*>", "");
        System.out.println("Contents of the web page: "+result);
        return result;
    }

}
