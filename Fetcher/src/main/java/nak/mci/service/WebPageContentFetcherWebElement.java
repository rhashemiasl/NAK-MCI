package nak.mci.service;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class WebPageContentFetcherWebElement implements WebPageContentFetcher {

    WebClient webClient;
    public WebPageContentFetcherWebElement()
    {
        webClient = new WebClient(BrowserVersion.BEST_SUPPORTED);
        webClient.getOptions().setJavaScriptEnabled(false);
    }

    @Override
    public String fetchContent(String url) throws IOException {

        HtmlPage htmlPage = webClient.getPage(url);
        return htmlPage.asNormalizedText();

    }
}
