package com.arcana.mod.client.gui;

import com.arcana.mod.ArcanaMod;
import com.arcana.mod.util.common.ArcanaLog;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class DiaryScreen extends Screen {
    private static final ResourceLocation COVER_FRONT = ResourceLocation.fromNamespaceAndPath(ArcanaMod.MODID, "textures/item/gui/diary_cover_front.png");
    private static final ResourceLocation COVER_BACK = ResourceLocation.fromNamespaceAndPath(ArcanaMod.MODID, "textures/item/gui/diary_cover_back.png");
    private static final ResourceLocation PAGE_BACKGROUND = ResourceLocation.fromNamespaceAndPath(ArcanaMod.MODID, "textures/item/gui/diary_page.png");
    private static final String MODULE = "DIARY";
    private static final int TEXTURE_WIDTH = 174;
    private static final int TEXTURE_HEIGHT = 256;
    private static final int TEXT_MARGIN = 15;
    private static final int TEXT_WIDTH = TEXTURE_WIDTH - (TEXT_MARGIN * 2);
    private static final int TEXT_HEIGHT = TEXTURE_HEIGHT - (TEXT_MARGIN * 2);
    private static final int LINE_HEIGHT = 11;
    private static final int TEXT_COLOR = 0x56463f;
    private static final int PAGE_NUMBER_COLOR = 0x6B5744;
    private static final float FADE_SPEED = 0.1F;

    private static final int ICON_DRAW_SIZE = 32;
    private static final int ICON_DRAW_SPACING = 10;
    private static final int ICON_DRAW_PADDING = 4;
    private static final int ICON_ROW_HEIGHT = ICON_DRAW_SIZE + ICON_DRAW_PADDING * 2;

    private interface InlineRun {}
    private record TextRun(FormattedCharSequence seq, int pixelWidth) implements InlineRun {}
    private record IllustrationRun(List<ResourceLocation> items) implements InlineRun {}

    private record DiaryPageLine(List<InlineRun> runs, int height) {}

    private int leftPos;
    private int topPos;
    private int currentPage = 0;
    private List<List<DiaryPageLine>> contentPages;
    private int totalPages;
    private float pageAlpha = 0.0F;

    public DiaryScreen() {
        super(Component.literal(DiaryContent.getTitle()));
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - TEXTURE_WIDTH) / 2;
        this.topPos = (this.height - TEXTURE_HEIGHT) / 2;

        if (contentPages == null) {
            ArcanaLog.debug(MODULE, "Processing diary content");
            DiaryContent.DiaryBook book = DiaryContent.load();
            this.contentPages = buildPages(book);
            this.totalPages = contentPages.size() + 2;
            ArcanaLog.debug(MODULE, "Total pages: " + this.totalPages);
        }
    }

    private class PageLayout {
        final List<List<DiaryPageLine>> pages = new ArrayList<>();
        final List<DiaryPageLine> currentPage = new ArrayList<>();
        final List<InlineRun> currentLine = new ArrayList<>();
        int lineWidth = 0;

        private int pageUsedHeight() {
            return currentPage.stream().mapToInt(DiaryPageLine::height).sum();
        }

        private void pushLine(List<InlineRun> runs, int height) {
            if (pageUsedHeight() + height > TEXT_HEIGHT) {
                pages.add(new ArrayList<>(currentPage));
                currentPage.clear();
            }
            currentPage.add(new DiaryPageLine(runs, height));
        }

        void addText(Component comp) {
            String plain = comp.getString();
            Style style = comp.getSiblings().isEmpty() ? comp.getStyle() : comp.getSiblings().getFirst().getStyle();

            while (!plain.isEmpty()) {
                int remaining = TEXT_WIDTH - lineWidth;
                if (remaining <= 0) {
                    flushLine();
                    continue;
                }

                int end = findBreak(plain, style, remaining);

                if (end == 0) {
                    if (lineWidth > 0) { flushLine(); continue; }
                    end = 1;
                }

                boolean wraps = end < plain.length();
                String chunk = plain.substring(0, end);
                plain = wraps ? plain.substring(end).stripLeading() : "";

                var splitResult = font.split(Component.literal(chunk).withStyle(style), TEXT_WIDTH);
                if (!splitResult.isEmpty()) {
                    FormattedCharSequence seq = splitResult.getFirst();
                    int w = font.width(seq);
                    currentLine.add(new TextRun(seq, w));
                    lineWidth += w;
                }

                if (wraps) flushLine();
            }
        }

        void flushLine() {
            if (currentLine.isEmpty()) return;
            pushLine(new ArrayList<>(currentLine), LINE_HEIGHT);
            currentLine.clear();
            lineWidth = 0;
        }

        void separator() {
            if (!currentLine.isEmpty()) flushLine();
            pushLine(List.of(), LINE_HEIGHT);
        }

        void addIconRow(List<ResourceLocation> icons) {
            if (icons.isEmpty()) return;
            if (!currentLine.isEmpty()) flushLine();
            pushLine(List.of(new IllustrationRun(icons)), ICON_ROW_HEIGHT);
        }

        void startNewPage() {
            if (!currentLine.isEmpty()) flushLine();
            if (!currentPage.isEmpty()) {
                pages.add(new ArrayList<>(currentPage));
                currentPage.clear();
            }
        }

        List<List<DiaryPageLine>> finish() {
            if (!currentLine.isEmpty()) flushLine();
            if (!currentPage.isEmpty()) pages.add(new ArrayList<>(currentPage));
            return pages;
        }

        private int findBreak(String text, Style style, int maxWidth) {
            int lastWordEnd = 0;
            for (int i = 0; i < text.length(); i++) {
                int w = font.width(Component.literal(text.substring(0, i + 1)).withStyle(style));
                if (w > maxWidth) return lastWordEnd > 0 ? lastWordEnd : i;
                if (text.charAt(i) == ' ') lastWordEnd = i + 1;
            }
            return text.length();
        }
    }

    private List<List<DiaryPageLine>> buildPages(DiaryContent.DiaryBook book) {
        PageLayout layout = new PageLayout();

        for (Component para : book.intro()) {
            layout.addText(para);
            layout.separator();
        }

        for (DiaryContent.DiaryDay day : book.days()) {
            layout.startNewPage();
            layout.addText(Component.literal(day.date()).withStyle(Style.EMPTY.withBold(true)));
            layout.separator();

            for (Component para : day.paragraphs()) {
                layout.addText(para);
                layout.separator();
            }

            if (!day.icons().isEmpty()) {
                layout.addIconRow(day.icons());
            }
        }

        return layout.finish();
    }

    private void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            pageAlpha = 0.0F;
            ArcanaLog.debug(MODULE, "Previous page: " + currentPage);
            playPageTurnSound();
        }
    }

    private void nextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            pageAlpha = 0.0F;
            ArcanaLog.debug(MODULE, "Next page: " + currentPage);
            playPageTurnSound();
        }
    }

    private void playPageTurnSound() {
        var p = Minecraft.getInstance().player;
        if (p != null) p.playSound(SoundEvents.BOOK_PAGE_TURN, 0.75F, 1.0F);
    }

    private boolean isFrontCover() { return currentPage == 0; }
    private boolean isBackCover()  { return currentPage == totalPages - 1; }
    private boolean isContentPage(){ return currentPage > 0 && currentPage < totalPages - 1; }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderDarkOverlay(graphics);
        renderDiaryBackground(graphics);
        if (pageAlpha < 1.0F) pageAlpha = Math.min(1.0F, pageAlpha + FADE_SPEED);
        if (isContentPage()) {
            renderPageContent(graphics);
            renderPageNumber(graphics);
        }
        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    private void renderDarkOverlay(GuiGraphics graphics) {
        graphics.fill(0, 0, this.width, this.height, 0xA0000000);
    }

    private void renderDiaryBackground(GuiGraphics graphics) {
        ResourceLocation bg = isFrontCover() ? COVER_FRONT : isBackCover() ? COVER_BACK : PAGE_BACKGROUND;
        int cx = (this.width - 256) / 2;
        int cy = (this.height - 256) / 2;
        graphics.blit(bg, cx, cy, 0, 0, 256, 256, 256, 256);
    }

    private int applyAlpha(int color) {
        int alpha = (int)(pageAlpha * 255) & 0xFF;
        return (alpha << 24) | (color & 0x00FFFFFF);
    }

    private void renderPageContent(GuiGraphics graphics) {
        int index = currentPage - 1;
        if (index < 0 || index >= contentPages.size()) return;

        List<DiaryPageLine> lines = contentPages.get(index);
        int baseX = leftPos + TEXT_MARGIN;
        int y = topPos + TEXT_MARGIN;

        for (DiaryPageLine line : lines) {
            if (line.runs().size() == 1 && line.runs().getFirst() instanceof IllustrationRun(
                    List<ResourceLocation> items
            )) {
                renderIllustrationRow(graphics, baseX, y, line.height(), items);
            } else {
                int x = baseX;
                for (InlineRun run : line.runs()) {
                    if (run instanceof TextRun(FormattedCharSequence seq, int pixelWidth)) {
                        graphics.drawString(this.font, seq, x, y, applyAlpha(TEXT_COLOR), false);
                        x += pixelWidth;
                    }
                }
            }
            y += line.height();
        }
    }

    private void renderIllustrationRow(GuiGraphics graphics, int baseX, int y, int rowHeight, List<ResourceLocation> items) {
        int totalWidth = items.size() * ICON_DRAW_SIZE + Math.max(0, items.size() - 1) * ICON_DRAW_SPACING;
        int startX = baseX + (TEXT_WIDTH - totalWidth) / 2;
        int iconY = y + (rowHeight - ICON_DRAW_SIZE) / 2;

        int x = startX;
        for (ResourceLocation itemLoc : items) {
            var optItem = BuiltInRegistries.ITEM.getOptional(itemLoc);
            if (optItem.isEmpty()) {
                ArcanaLog.debug(MODULE, "Item not found for diary illustration: {}", itemLoc);
                x += ICON_DRAW_SIZE + ICON_DRAW_SPACING;
                continue;
            }
            ItemStack stack = new ItemStack(optItem.get());
            if (!stack.isEmpty()) {
                float scale = (float) ICON_DRAW_SIZE / 16f;
                graphics.pose().pushPose();
                graphics.pose().translate(x, iconY, 0);
                graphics.pose().scale(scale, scale, 1.0f);
                graphics.renderItem(stack, 0, 0);
                graphics.pose().popPose();
            }
            x += ICON_DRAW_SIZE + ICON_DRAW_SPACING;
        }
    }

    private void renderPageNumber(GuiGraphics graphics) {
        String text = currentPage + " / " + (totalPages - 2);
        int w = this.font.width(text);
        graphics.drawString(this.font, text, leftPos + (TEXTURE_WIDTH - w) / 2, topPos + TEXTURE_HEIGHT - TEXT_MARGIN, applyAlpha(PAGE_NUMBER_COLOR), false);
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {}

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (handlePageNavigation(keyCode)) return true;
        if (handlePageJump(keyCode)) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private boolean handlePageNavigation(int keyCode) {
        if (keyCode == InputConstants.KEY_LEFT || keyCode == InputConstants.KEY_A) { previousPage(); return true; }
        if (keyCode == InputConstants.KEY_RIGHT || keyCode == InputConstants.KEY_D) { nextPage(); return true; }
        return false;
    }

    private boolean handlePageJump(int keyCode) {
        int digit = mapKeyToDigit(keyCode);
        if (digit == -1) return false;
        int target = digit == 0 ? 1 : digit;
        int max = totalPages - 2;
        if (target < 1 || target > max) return false;
        ArcanaLog.debug(MODULE, "Jumping to page: " + target);
        currentPage = target;
        pageAlpha = 0.0F;
        playPageTurnSound();
        return true;
    }

    private int mapKeyToDigit(int keyCode) {
        if (keyCode >= InputConstants.KEY_1 && keyCode <= InputConstants.KEY_9) return keyCode - InputConstants.KEY_1 + 1;
        if (keyCode == InputConstants.KEY_0) return 0;
        return -1;
    }
}
