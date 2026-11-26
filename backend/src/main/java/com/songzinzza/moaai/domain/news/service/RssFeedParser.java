package com.songzinzza.moaai.domain.news.service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.HtmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RssFeedParser {
  private static final ZoneId KST = ZoneId.of("Asia/Seoul");
  private final RestClient restClient;

  public RssFeedParser() {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(Duration.ofSeconds(5));
    factory.setReadTimeout(Duration.ofSeconds(10));

    this.restClient = RestClient.builder()
        .requestFactory(factory)
        .defaultHeader("User-Agent", "MoaAI-NewsCollector/1.0")
        .build();
  }

  public List<RssFeedItem> fetch(String sourceName, String feedUrl, int maxItems) {
    try {
      String xml = restClient.get()
          .uri(feedUrl)
          .retrieve()
          .body(String.class);

      if (xml == null || xml.isBlank())
        return Collections.emptyList();

      return parseXml(sourceName, xml, maxItems);
    } catch (Exception e) {
      log.warn("RSS 피드 수집 실패 - source={}, url={}, reason={}", sourceName, feedUrl, e.getMessage());

      return Collections.emptyList();
    }
  }

  private List<RssFeedItem> parseXml(String sourceName, String xml, int maxItems) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    factory.setXIncludeAware(false);
    factory.setExpandEntityReferences(false);

    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

    NodeList nodes = doc.getElementsByTagName("item");
    boolean isAtom = false;
    if (nodes.getLength() == 0) {
      nodes = doc.getElementsByTagName("entry");
      isAtom = true;
    }

    List<RssFeedItem> items = new ArrayList<>();
    int limit = Math.min(nodes.getLength(), maxItems);

    for (int i = 0; i < limit; i++) {
      Element el = (Element) nodes.item(i);
      String title = cleanText(getTagText(el, "title"));
      String link = isAtom ? getAtomLink(el) : cleanText(getTagText(el, "link"));
      String rawDesc = isAtom
          ? firstNonBlank(getTagText(el, "summary"), getTagText(el, "content"))
          : firstNonBlank(getTagText(el, "description"), getTagText(el, "content:encoded"));
      String description = cleanText(rawDesc);
      String pubDateStr = isAtom
          ? firstNonBlank(getTagText(el, "published"), getTagText(el, "updated"))
          : getTagText(el, "pubDate");

      if (!title.isBlank() && !link.isBlank())
        items.add(new RssFeedItem(title, link, description, sourceName, parseDate(pubDateStr)));
    }

    return items;
  }

  private String getTagText(Element parent, String tagName) {
    NodeList list = parent.getElementsByTagName(tagName);
    if (list.getLength() == 0 || list.item(0) == null)
      return "";

    return list.item(0).getTextContent();
  }

  private String getAtomLink(Element entry) {
    NodeList links = entry.getElementsByTagName("link");
    for (int i = 0; i < links.getLength(); i++) {
      Element linkEl = (Element) links.item(i);
      String href = linkEl.getAttribute("href");

      if (!href.isBlank())
        return href.trim();
    }

    return "";
  }

  private String firstNonBlank(String a, String b) {
    return (a != null && !a.isBlank()) ? a : (b != null ? b : "");
  }

  private String cleanText(String text) {
    if (text == null)
      return "";

    return HtmlUtils.htmlUnescape(text)
        .replaceAll("<[^>]*>", " ")
        .replace('\u00A0', ' ')
        .replaceAll("\\s+", " ")
        .trim();
  }

  private LocalDateTime parseDate(String dateStr) {
    if (dateStr == null || dateStr.isBlank())
      return LocalDateTime.now(KST);

    String trimmed = dateStr.trim();

    try {
      return ZonedDateTime.parse(trimmed, DateTimeFormatter.RFC_1123_DATE_TIME)
          .withZoneSameInstant(KST)
          .toLocalDateTime();
    } catch (Exception ignored) {
    }

    try {
      return ZonedDateTime.parse(trimmed, DateTimeFormatter.ISO_DATE_TIME)
          .withZoneSameInstant(KST)
          .toLocalDateTime();
    } catch (Exception ignored) {
    }

    return LocalDateTime.now(KST);
  }

  public record RssFeedItem(
      String title,
      String link,
      String description,
      String sourceName,
      LocalDateTime publishedAt) {
  }
}
