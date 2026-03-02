"use client"

import { useState, useCallback } from "react"
import { RotateCcw } from "lucide-react"
import { initialFamilyData, initialMorningItems, DEFAULT_CATEGORIES, CATEGORY_COLORS } from "@/lib/packing-data"
import type { FamilyMember, PackingItem } from "@/lib/packing-data"
import { MemberSelector } from "./member-selector"
import { ProgressHeader } from "./progress-header"
import { CategoryGroup } from "./category-group"
import { MorningListView } from "./morning-list"
import { DoneSection } from "./done-section"
import { AddItemForm } from "./add-item-form"
import { CategoryManager } from "./category-manager"

export function PackingList() {
  const [family, setFamily] = useState<FamilyMember[]>(() =>
    JSON.parse(JSON.stringify(initialFamilyData))
  )
  const [morningOnlyItems, setMorningOnlyItems] = useState<PackingItem[]>(() =>
    JSON.parse(JSON.stringify(initialMorningItems))
  )
  const [categories, setCategories] = useState<string[]>(() => [...DEFAULT_CATEGORIES])
  const [activeMemberId, setActiveMemberId] = useState(initialFamilyData[0].id)
  const [showResetConfirm, setShowResetConfirm] = useState(false)

  const isMorningView = activeMemberId === "morning"
  const activeMember = family.find((m) => m.id === activeMemberId)

  const allSnoozedItems = family.flatMap((m) =>
    m.items.filter((i) => i.status === "snoozed").map((i) => ({ ...i, ownerId: m.id }))
  )
  const allMorningItems = [...allSnoozedItems, ...morningOnlyItems]

  // ---------- Member item helpers ----------
  const updateMemberItems = useCallback(
    (memberId: string, updater: (items: FamilyMember["items"]) => FamilyMember["items"]) => {
      setFamily((prev) =>
        prev.map((m) => (m.id === memberId ? { ...m, items: updater(m.items) } : m))
      )
    },
    []
  )

  const toggleDone = useCallback(
    (itemId: string) => {
      if (!activeMember) return
      updateMemberItems(activeMember.id, (items) =>
        items.map((item) =>
          item.id === itemId
            ? { ...item, status: item.status === "done" ? "todo" : "done" }
            : item
        )
      )
    },
    [activeMember, updateMemberItems]
  )

  const snoozeItem = useCallback(
    (itemId: string) => {
      if (!activeMember) return
      updateMemberItems(activeMember.id, (items) =>
        items.map((item) =>
          item.id === itemId ? { ...item, status: "snoozed" } : item
        )
      )
    },
    [activeMember, updateMemberItems]
  )

  const addItem = useCallback(
    (name: string, category: string) => {
      if (!activeMember) return
      const newItem: PackingItem = {
        id: `${activeMember.id}-${Date.now()}-${Math.random().toString(36).slice(2, 6)}`,
        name,
        category,
        status: "todo",
        ownerId: activeMember.id,
      }
      updateMemberItems(activeMember.id, (items) => [...items, newItem])
    },
    [activeMember, updateMemberItems]
  )

  const editItem = useCallback(
    (itemId: string, newName: string) => {
      if (!activeMember) return
      updateMemberItems(activeMember.id, (items) =>
        items.map((item) => (item.id === itemId ? { ...item, name: newName } : item))
      )
    },
    [activeMember, updateMemberItems]
  )

  const deleteItem = useCallback(
    (itemId: string) => {
      if (!activeMember) return
      updateMemberItems(activeMember.id, (items) => items.filter((item) => item.id !== itemId))
    },
    [activeMember, updateMemberItems]
  )

  // ---------- Morning callbacks ----------
  const morningToggleDone = useCallback(
    (itemId: string, memberId?: string) => {
      if (memberId) {
        updateMemberItems(memberId, (items) =>
          items.map((item) =>
            item.id === itemId
              ? { ...item, status: item.status === "done" ? "snoozed" : "done" }
              : item
          )
        )
      }
    },
    [updateMemberItems]
  )

  const unsnoozeItem = useCallback(
    (itemId: string, memberId: string) => {
      updateMemberItems(memberId, (items) =>
        items.map((item) =>
          item.id === itemId ? { ...item, status: "todo" } : item
        )
      )
    },
    [updateMemberItems]
  )

  const addMorningItem = useCallback((name: string, category: string) => {
    const newItem: PackingItem = {
      id: `morning-${Date.now()}-${Math.random().toString(36).slice(2, 6)}`,
      name,
      category,
      status: "todo",
    }
    setMorningOnlyItems((prev) => [...prev, newItem])
  }, [])

  const editMorningOnly = useCallback((id: string, newName: string) => {
    setMorningOnlyItems((prev) =>
      prev.map((item) => (item.id === id ? { ...item, name: newName } : item))
    )
  }, [])

  const deleteMorningOnly = useCallback((id: string) => {
    setMorningOnlyItems((prev) => prev.filter((item) => item.id !== id))
  }, [])

  const toggleMorningOnlyDone = useCallback((id: string) => {
    setMorningOnlyItems((prev) =>
      prev.map((item) =>
        item.id === id
          ? { ...item, status: item.status === "done" ? "todo" : "done" }
          : item
      )
    )
  }, [])

  // ---------- Category management ----------
  const addCategory = useCallback((name: string) => {
    setCategories((prev) => [...prev, name])
    // Also add a color mapping
    CATEGORY_COLORS[name] = "bg-primary/10 text-primary"
  }, [])

  const renameCategory = useCallback((oldName: string, newName: string) => {
    setCategories((prev) => prev.map((c) => (c === oldName ? newName : c)))
    // Update all items across all members
    setFamily((prev) =>
      prev.map((m) => ({
        ...m,
        items: m.items.map((item) =>
          item.category === oldName ? { ...item, category: newName } : item
        ),
      }))
    )
    setMorningOnlyItems((prev) =>
      prev.map((item) => (item.category === oldName ? { ...item, category: newName } : item))
    )
    CATEGORY_COLORS[newName] = CATEGORY_COLORS[oldName] || "bg-primary/10 text-primary"
    delete CATEGORY_COLORS[oldName]
  }, [])

  const deleteCategory = useCallback((name: string) => {
    setCategories((prev) => prev.filter((c) => c !== name))
    // Move items in that category to "General"
    setFamily((prev) =>
      prev.map((m) => ({
        ...m,
        items: m.items.map((item) =>
          item.category === name ? { ...item, category: "General" } : item
        ),
      }))
    )
    setMorningOnlyItems((prev) =>
      prev.map((item) => (item.category === name ? { ...item, category: "General" } : item))
    )
  }, [])

  // ---------- Global reset ----------
  const resetAll = useCallback(() => {
    setFamily(JSON.parse(JSON.stringify(initialFamilyData)))
    setMorningOnlyItems(JSON.parse(JSON.stringify(initialMorningItems)))
    setCategories([...DEFAULT_CATEGORIES])
    setShowResetConfirm(false)
  }, [])

  // ---------- Derived data ----------
  const todoItems = activeMember?.items.filter((i) => i.status === "todo") ?? []
  const doneItems = activeMember?.items.filter((i) => i.status === "done") ?? []

  const allCats = activeMember
    ? [...new Set(activeMember.items.filter((i) => i.status !== "snoozed").map((i) => i.category))]
    : []

  const categoriesWithItems = allCats.map((cat) => ({
    category: cat,
    items: activeMember!.items.filter((i) => i.category === cat && i.status !== "snoozed"),
  }))

  const sortedCategories = categoriesWithItems.sort((a, b) => {
    const aHasTodo = a.items.some((i) => i.status === "todo")
    const bHasTodo = b.items.some((i) => i.status === "todo")
    if (aHasTodo && !bHasTodo) return -1
    if (!aHasTodo && bHasTodo) return 1
    return 0
  })

  return (
    <div className="flex min-h-full flex-col">
      {/* MD3 Top App Bar */}
      <header className="sticky top-0 z-20 bg-background/95 backdrop-blur-sm">
        <div className="flex items-center justify-between px-4 pb-2 pt-3">
          <div>
            <h1 className="text-xl font-semibold tracking-tight text-foreground">PackUp</h1>
            <p className="text-[11px] text-muted-foreground">Family packing lists</p>
          </div>

          {showResetConfirm ? (
            <div className="flex items-center gap-2">
              <span className="text-[11px] text-destructive">Reset everything?</span>
              <button
                onClick={resetAll}
                className="rounded-full bg-destructive/10 px-3 py-1.5 text-xs font-medium text-destructive transition-colors hover:bg-destructive/20 active:scale-95"
              >
                Reset
              </button>
              <button
                onClick={() => setShowResetConfirm(false)}
                className="rounded-full px-3 py-1.5 text-xs font-medium text-muted-foreground hover:bg-muted"
              >
                Cancel
              </button>
            </div>
          ) : (
            <button
              onClick={() => setShowResetConfirm(true)}
              className="flex items-center gap-1.5 rounded-full p-2 text-muted-foreground transition-colors hover:bg-muted hover:text-foreground active:scale-95"
              aria-label="Reset all lists to original state"
            >
              <RotateCcw className="h-5 w-5" />
            </button>
          )}
        </div>

        {/* Member selector */}
        <MemberSelector
          members={family}
          activeMemberId={activeMemberId}
          morningItems={allMorningItems}
          onSelect={setActiveMemberId}
        />
        <div className="mx-4 mt-2 h-px bg-border/40" />
      </header>

      {/* Content */}
      <div className="flex-1 overflow-y-auto pb-8">
        {isMorningView ? (
          <div className="pt-4">
            <MorningListView
              family={family}
              morningOnlyItems={morningOnlyItems}
              onToggleDone={morningToggleDone}
              onUnsnooze={unsnoozeItem}
              onEditMorningOnly={editMorningOnly}
              onDeleteMorningOnly={deleteMorningOnly}
              onAddMorningItem={addMorningItem}
              onToggleMorningOnlyDone={toggleMorningOnlyDone}
            />
          </div>
        ) : activeMember ? (
          <div className="flex flex-col gap-3 px-4 pt-4">
            {/* Progress */}
            <ProgressHeader member={activeMember} />

            {/* Category groups */}
            {sortedCategories.map(({ category, items }) => (
              <CategoryGroup
                key={category}
                category={category}
                items={items}
                onToggleDone={toggleDone}
                onSnooze={snoozeItem}
                onEdit={editItem}
                onDelete={deleteItem}
              />
            ))}

            {/* Add item */}
            <AddItemForm onAdd={addItem} categories={categories} />

            {/* Category manager */}
            <CategoryManager
              categories={categories}
              onAddCategory={addCategory}
              onRenameCategory={renameCategory}
              onDeleteCategory={deleteCategory}
            />

            {/* Done items */}
            <DoneSection
              items={doneItems}
              onToggleDone={toggleDone}
              onEdit={editItem}
              onDelete={deleteItem}
            />
          </div>
        ) : null}
      </div>
    </div>
  )
}
