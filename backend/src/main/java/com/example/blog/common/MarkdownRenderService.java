package com.example.blog.common;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import org.springframework.stereotype.Component;

@Component
public class MarkdownRenderService {

    private final Parser parser = Parser.builder().build();
    private final HtmlRenderer renderer = HtmlRenderer.builder()
            .escapeHtml(true)
            .build();

    public String render(String markdown) {
        return renderer.render(parser.parse(markdown));
    }
}
