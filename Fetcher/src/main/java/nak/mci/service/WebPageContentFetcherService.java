package nak.mci.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;


/*
there are different ways to extract a webpage content
WebElement and URL scanner has been implemented
WebElement is set as default method.
 */
@Service
public class WebPageContentFetcherService {




    @Value("${webpage.fetchMethod:WebElement}")
    private String fetchMode;
    WebPageContentFetcherWebElement webPageContentFetcherWebElement;
    WebPageContentFetcherURL webPageContentFetcherURL;

    @Autowired
    public WebPageContentFetcherService(WebPageContentFetcherWebElement webPageContentFetcherWebElement, WebPageContentFetcherURL webPageContentFetcherURL) {
        this.webPageContentFetcherWebElement = webPageContentFetcherWebElement;
        this.webPageContentFetcherURL = webPageContentFetcherURL;
    }

    public String getWebPageContent(String url) throws IOException {
        if(fetchMode.equals("URL"))
            return webPageContentFetcherURL.fetchContent(url);
        else
            return webPageContentFetcherWebElement.fetchContent(url);
    }
}
