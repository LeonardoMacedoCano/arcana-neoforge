package com.example.arcana.client.gui;

import com.example.arcana.util.ArcanaLog;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DiaryScreen extends Screen {
    private static final ResourceLocation COVER_FRONT = ResourceLocation.fromNamespaceAndPath("arcana", "textures/item/gui/diary_cover_front.png");
    private static final ResourceLocation COVER_BACK = ResourceLocation.fromNamespaceAndPath("arcana", "textures/item/gui/diary_cover_back.png");
    private static final ResourceLocation PAGE_BACKGROUND = ResourceLocation.fromNamespaceAndPath("arcana", "textures/item/gui/diary_page.png");
    private static final String MODULE = "DIARY";
    private static final int TEXTURE_WIDTH = 174;
    private static final int TEXTURE_HEIGHT = 256;
    private static final int TEXT_MARGIN = 15;
    private static final int TEXT_WIDTH = TEXTURE_WIDTH - (TEXT_MARGIN * 2);
    private static final int TEXT_HEIGHT = TEXTURE_HEIGHT - (TEXT_MARGIN * 2);
    private static final int LINE_HEIGHT = 11;
    private static final int MAX_LINES_PER_PAGE = TEXT_HEIGHT / LINE_HEIGHT;
    private static final int TEXT_COLOR = 0x56463f;
    private static final int PAGE_NUMBER_COLOR = 0x6B5744;
    private static final float FADE_SPEED = 0.1F;

    private int leftPos;
    private int topPos;
    private int currentPage = 0;
    private List<List<FormattedCharSequence>> contentPages;
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
            this.contentPages = processContentIntoPages();
            this.totalPages = contentPages.size() + 2;
            ArcanaLog.debug(MODULE, "Total pages calculated: " + this.totalPages);
        }
    }

    private List<List<FormattedCharSequence>> processContentIntoPages() {
        List<List<FormattedCharSequence>> pages = new ArrayList<>();
        List<FormattedCharSequence> current = new ArrayList<>();
        String[] content = DiaryContent.getContent();

        for (String paragraph : content) {
            List<FormattedCharSequence> wrapped = this.font.split(Component.literal(paragraph), TEXT_WIDTH);
            for (FormattedCharSequence line : wrapped) {
                if (current.size() >= MAX_LINES_PER_PAGE) {
                    pages.add(new ArrayList<>(current));
                    current.clear();
                }
                current.add(line);
            }
            if (!paragraph.isEmpty() && current.size() < MAX_LINES_PER_PAGE) {
                current.add(FormattedCharSequence.EMPTY);
            }
        }

        if (!current.isEmpty()) {
            pages.add(current);
        }

        ArcanaLog.debug(MODULE, "Generated content pages: " + pages.size());
        return pages;
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
        var player = Minecraft.getInstance().player;
        if (player != null) {
            player.playSound(SoundEvents.BOOK_PAGE_TURN, 0.75F, 1.0F);
        }
    }

    private boolean isFrontCover() {
        return currentPage == 0;
    }

    private boolean isBackCover() {
        return currentPage == totalPages - 1;
    }

    private boolean isContentPage() {
        return currentPage > 0 && currentPage < totalPages - 1;
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderBackgroundBlur(graphics);
        renderDiaryBackground(graphics);

        if (pageAlpha < 1.0F) {
            pageAlpha = Math.min(1.0F, pageAlpha + FADE_SPEED);
        }

        if (isContentPage()) {
            renderPageContent(graphics);
            renderPageNumber(graphics);
        }

        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    private void renderBackgroundBlur(GuiGraphics graphics) {
        graphics.fill(0, 0, this.width, this.height, 0xA0000000);
    }

    private void renderDiaryBackground(GuiGraphics graphics) {
        ResourceLocation background = isFrontCover() ? COVER_FRONT : isBackCover() ? COVER_BACK : PAGE_BACKGROUND;
        int centerX = (this.width - 256) / 2;
        int centerY = (this.height - 256) / 2;
        graphics.blit(background, centerX, centerY, 0, 0, 256, 256, 256, 256);
    }

    private void renderPageContent(GuiGraphics graphics) {
        int index = currentPage - 1;
        if (index < 0 || index >= contentPages.size()) return;

        List<FormattedCharSequence> lines = contentPages.get(index);
        int x = leftPos + TEXT_MARGIN;
        int y = topPos + TEXT_MARGIN;

        for (FormattedCharSequence line : lines) {
            graphics.drawString(this.font, line, x, y, TEXT_COLOR, false);
            y += LINE_HEIGHT;
        }
    }

    private void renderPageNumber(GuiGraphics graphics) {
        int contentPageNumber = currentPage;
        int totalContentPages = totalPages - 2;
        String text = contentPageNumber + " / " + totalContentPages;
        int width = this.font.width(text);

        int x = leftPos + (TEXTURE_WIDTH - width) / 2;
        int y = topPos + TEXTURE_HEIGHT - TEXT_MARGIN;

        graphics.drawString(this.font, text, x, y, PAGE_NUMBER_COLOR, false);
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (handlePageNavigation(keyCode)) return true;
        if (handlePageJump(keyCode)) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private boolean handlePageNavigation(int keyCode) {
        if (isLeftKey(keyCode)) {
            previousPage();
            return true;
        }

        if (isRightKey(keyCode)) {
            nextPage();
            return true;
        }

        return false;
    }

    private boolean handlePageJump(int keyCode) {
        int digit = mapKeyToDigit(keyCode);
        if (digit == -1) return false;

        int targetPage = digit == 0 ? 1 : digit;
        return tryGoToPage(targetPage);
    }

    private boolean isLeftKey(int keyCode) {
        return keyCode == InputConstants.KEY_LEFT || keyCode == InputConstants.KEY_A;
    }

    private boolean isRightKey(int keyCode) {
        return keyCode == InputConstants.KEY_RIGHT || keyCode == InputConstants.KEY_D;
    }

    private boolean tryGoToPage(int page) {
        int minPage = 1;
        int maxPage = totalPages - 2;

        if (page < minPage || page > maxPage) return false;

        ArcanaLog.debug(MODULE, "Jumping to page: " + page);
        currentPage = page;
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
