package com.daqem.grieflogger.command.page;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.model.history.IHistory;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Page {

    public static final int MAX_PAGE_SIZE = 10;
    private final List<IHistory> history;
    private final int page;
    private final int maxPage;
    private final boolean singleLocation;

    public Page(List<IHistory> history, int page, int maxPage, boolean singleLocation) {
        this.history = history;
        this.page = page;
        this.maxPage = maxPage;
        this.singleLocation = singleLocation;
    }

    public List<Component> getHistory() {
        List<Component> components = new ArrayList<>();

        components.add(getHeader());

        for (IHistory history : this.history) {
            if (singleLocation) {
                components.add(history.getComponent());
            } else {
                components.add(history.getComponentWithPos());
            }
        }

        if (maxPage > 1) {
            components.add(getFooter());
        }

        return components;
    }

    private Component getHeader() {
        MutableComponent header = GriefLogger.translate("lookup.history_header", GriefLogger.themedTranslate("lookup.history_title"));
        if (singleLocation && !history.isEmpty()) {
            header.append(" ").append(history.get(0).getPosition().getComponent());
        }
        return header;
    }

    private Component getFooter() {
        return getArrowLeft().append(" ")
                .append(GriefLogger.themedTranslate("lookup.page")).append(" ")
                .append(GriefLogger.translate("lookup.pages", page, maxPage).withStyle(ChatFormatting.WHITE)).append(" ")
                .append(getArrowRight());
    }

    private MutableComponent getArrowLeft() {

        return GriefLogger.literal("⯇").withStyle(getStyle(page - 1, page > 1));
    }

    private MutableComponent getArrowRight() {
        return GriefLogger.literal("⯈").withStyle(getStyle(page + 1, page < maxPage));
    }

    private ClickEvent getClickEvent(int page) {
        return new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/grieflogger page " + page);
    }

    private Style getStyle(int page, boolean enabled) {
        Style style = Style.EMPTY.withClickEvent(getClickEvent(page)).withColor(ChatFormatting.WHITE);
        if (!enabled) {
            style = Style.EMPTY.withColor(ChatFormatting.GRAY);
        }
        return style;
    }

    public void sendToPlayer(ServerPlayer serverPlayer) {
        getHistory().forEach(serverPlayer::sendSystemMessage);
    }

    public static List<Page> convertToPages(List<IHistory> history, boolean singleLocation) {
        List<Page> pages = new ArrayList<>();
        int maxPage = (int) Math.ceil((double) history.size() / (double) MAX_PAGE_SIZE);
        int page = 1;
        for (int i = 0; i < history.size(); i += MAX_PAGE_SIZE) {
            int finalPage = page;
            pages.add(history.subList(i, Math.min(i + MAX_PAGE_SIZE, history.size()))
                    .stream()
                    .collect(Collectors.collectingAndThen(Collectors.toList(), x -> new Page(x, finalPage, maxPage, singleLocation))));
            page++;
        }
        return pages;
    }
}
