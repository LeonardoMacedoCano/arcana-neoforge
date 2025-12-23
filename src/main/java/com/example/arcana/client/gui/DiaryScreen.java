package com.example.arcana.client.gui;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DiaryScreen extends Screen {

    private static final ResourceLocation COVER_FRONT = ResourceLocation.fromNamespaceAndPath(
            "arcana",
            "textures/item/gui/diary_cover_front.png"
    );

    private static final ResourceLocation COVER_BACK = ResourceLocation.fromNamespaceAndPath(
            "arcana",
            "textures/item/gui/diary_cover_back.png"
    );

    private static final ResourceLocation PAGE_BACKGROUND = ResourceLocation.fromNamespaceAndPath(
            "arcana",
            "textures/item/gui/diary_page.png"
    );

    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;

    private static final int TEXT_MARGIN_LEFT = 30;
    private static final int TEXT_MARGIN_RIGHT = 30;
    private static final int TEXT_MARGIN_TOP = 35;
    private static final int TEXT_MARGIN_BOTTOM = 50;
    private static final int TEXT_WIDTH = TEXTURE_WIDTH - TEXT_MARGIN_LEFT - TEXT_MARGIN_RIGHT;
    private static final int TEXT_HEIGHT = TEXTURE_HEIGHT - TEXT_MARGIN_TOP - TEXT_MARGIN_BOTTOM;
    private static final int LINE_HEIGHT = 13;
    private static final int MAX_LINES_PER_PAGE = TEXT_HEIGHT / LINE_HEIGHT;

    private static final int TEXT_COLOR = 0x2C1810;
    private static final int PAGE_NUMBER_COLOR = 0x6B5744;

    private int leftPos;
    private int topPos;

    private int currentPage = 0;
    private List<List<FormattedCharSequence>> contentPages;
    private int totalPages;

    private Button previousButton;
    private Button nextButton;

    private float pageAlpha = 0.0F;
    private static final float FADE_SPEED = 0.1F;

    public DiaryScreen() {
        super(Component.literal(DiaryContent.getTitle()));
    }

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - TEXTURE_WIDTH) / 2;
        this.topPos = (this.height - TEXTURE_HEIGHT) / 2;

        if (this.contentPages == null) {
            this.contentPages = processContentIntoPages();
            this.totalPages = 1 + contentPages.size() + 1;
        }

        createNavigationButtons();
        updateButtonStates();
    }

    private void createNavigationButtons() {
        this.previousButton = Button.builder(
                        Component.literal("◄ Previous"),
                        button -> previousPage()
                )
                .bounds(leftPos + 15, topPos + TEXTURE_HEIGHT - 28, 70, 18)
                .build();

        this.nextButton = Button.builder(
                        Component.literal("Next ►"),
                        button -> nextPage()
                )
                .bounds(leftPos + TEXTURE_WIDTH - 85, topPos + TEXTURE_HEIGHT - 28, 70, 18)
                .build();

        this.addRenderableWidget(previousButton);
        this.addRenderableWidget(nextButton);
    }

    private List<List<FormattedCharSequence>> processContentIntoPages() {
        List<List<FormattedCharSequence>> allPages = new ArrayList<>();
        List<FormattedCharSequence> currentPageContent = new ArrayList<>();

        String[] content = DiaryContent.getContent();

        for (String paragraph : content) {
            List<FormattedCharSequence> wrappedLines = this.font.split(
                    Component.literal(paragraph),
                    TEXT_WIDTH
            );

            for (FormattedCharSequence line : wrappedLines) {
                if (currentPageContent.size() >= MAX_LINES_PER_PAGE) {
                    allPages.add(new ArrayList<>(currentPageContent));
                    currentPageContent.clear();
                }
                currentPageContent.add(line);
            }

            if (!paragraph.isEmpty() && currentPageContent.size() < MAX_LINES_PER_PAGE) {
                currentPageContent.add(FormattedCharSequence.EMPTY);
            }
        }

        if (!currentPageContent.isEmpty()) {
            allPages.add(currentPageContent);
        }

        return allPages;
    }

    private void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            pageAlpha = 0.0F;
            updateButtonStates();
            playPageTurnSound();
        }
    }

    private void nextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            pageAlpha = 0.0F;
            updateButtonStates();
            playPageTurnSound();
        }
    }

    private void playPageTurnSound() {
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.playSound(
                    SoundEvents.BOOK_PAGE_TURN,
                    0.75F,
                    1.0F
            );
        }
    }

    private void updateButtonStates() {
        previousButton.active = currentPage > 0;
        nextButton.active = currentPage < totalPages - 1;
    }

    private boolean isFrontCover() {
        return currentPage == 0;
    }

    private boolean isBackCover() {
        return currentPage == totalPages - 1;
    }

    private boolean isContentPage() {
        return !isFrontCover() && !isBackCover();
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
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

    private void renderDiaryBackground(GuiGraphics graphics) {
        ResourceLocation background;

        if (isFrontCover()) {
            background = COVER_FRONT;
        } else if (isBackCover()) {
            background = COVER_BACK;
        } else {
            background = PAGE_BACKGROUND;
        }

        graphics.blit(
                background,
                leftPos,
                topPos,
                0,
                0,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT
        );
    }

    private void renderPageContent(GuiGraphics graphics) {
        int contentIndex = currentPage - 1;

        if (contentIndex < 0 || contentIndex >= contentPages.size()) {
            return;
        }

        List<FormattedCharSequence> pageLines = contentPages.get(contentIndex);
        int textX = leftPos + TEXT_MARGIN_LEFT;
        int textY = topPos + TEXT_MARGIN_TOP;

        for (FormattedCharSequence line : pageLines) {
            graphics.drawString(
                    this.font,
                    line,
                    textX,
                    textY,
                    TEXT_COLOR,
                    false
            );
            textY += LINE_HEIGHT;
        }
    }

    private void renderPageNumber(GuiGraphics graphics) {
        if (!isContentPage()) return;

        int contentPageNumber = currentPage;
        int totalContentPages = totalPages - 1;

        String pageNumber = contentPageNumber + " / " + totalContentPages;
        int pageNumWidth = this.font.width(pageNumber);
        int pageNumX = leftPos + (TEXTURE_WIDTH - pageNumWidth) / 2;
        int pageNumY = topPos + TEXTURE_HEIGHT - 40;

        graphics.drawString(
                this.font,
                pageNumber,
                pageNumX,
                pageNumY,
                PAGE_NUMBER_COLOR,
                false
        );
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputConstants.KEY_LEFT || keyCode == InputConstants.KEY_A) {
            previousPage();
            return true;
        } else if (keyCode == InputConstants.KEY_RIGHT || keyCode == InputConstants.KEY_D) {
            nextPage();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}