package com.msrp.backend.util

class ItemNotFoundException : RuntimeException("Item not found")

class NoItemsAvailableException : RuntimeException("No items available for today")
