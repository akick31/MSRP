package com.msrp.backend.util

class ItemNotFoundException : RuntimeException("Item not found")

class NoItemsAvailableException : RuntimeException("No items available for today")

class ItemNotFromTodayException : RuntimeException("Item is not from today's game")
