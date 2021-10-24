package nak.mci.service;

import java.io.IOException;

public interface WebPageContentFetcher {

    String fetchContent(String url) throws IOException;


}
