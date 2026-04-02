package com.novaco.luxapi.commons.gui

import com.novaco.luxapi.commons.player.LuxPlayer

/**
 * An extension of LuxGui that supports multi-page content distribution.
 * Automatically handles page switching and item chunking.
 */
interface PaginatedGui : Gui {

    /**
     * Navigates the view to a specific page index.
     */
    fun setPage(player: LuxPlayer, page: Int)

    /**
     * Retrieves the current page index being viewed by the specific player.
     */
    fun getCurrentPage(player: LuxPlayer): Int

    /**
     * Calculates the total number of pages based on global item count.
     */
    fun getTotalPages(): Int
}