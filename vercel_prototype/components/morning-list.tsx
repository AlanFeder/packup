"use client"

import Image from "next/image"
import { Sunrise } from "lucide-react"
import type { FamilyMember, PackingItem as PackingItemType } from "@/lib/packing-data"
import { PackingItem } from "./packing-item"
import { AddItemForm } from "./add-item-form"

interface MorningListViewProps {
  family: FamilyMember[]
  morningOnlyItems: PackingItemType[]
  onToggleDone: (itemId: string, memberId?: string) => void
  onUnsnooze: (itemId: string, memberId: string) => void
  onEditMorningOnly: (id: string, newName: string) => void
  onDeleteMorningOnly: (id: string) => void
  onAddMorningItem: (name: string, category: string) => void
  onToggleMorningOnlyDone: (id: string) => void
}

export function MorningListView({
  family,
  morningOnlyItems,
  onToggleDone,
  onUnsnooze,
  onEditMorningOnly,
  onDeleteMorningOnly,
  onAddMorningItem,
  onToggleMorningOnlyDone,
}: MorningListViewProps) {
  const memberSnoozed = family
    .map((m) => ({
      member: m,
      items: m.items.filter((i) => i.status === "snoozed"),
    }))
    .filter((g) => g.items.length > 0)

  const morningTodo = morningOnlyItems.filter((i) => i.status === "todo")
  const morningDone = morningOnlyItems.filter((i) => i.status === "done")

  const totalSnoozed = memberSnoozed.reduce((s, g) => s + g.items.length, 0)
  const totalMorning = totalSnoozed + morningOnlyItems.length

  return (
    <div className="flex flex-col gap-4 px-4 pb-6">
      {/* Header */}
      <div className="flex items-center gap-3 py-2">
        <div className="flex h-12 w-12 items-center justify-center rounded-full bg-warning/15">
          <Sunrise className="h-6 w-6 text-warning-foreground" />
        </div>
        <div>
          <h2 className="text-lg font-semibold tracking-tight text-foreground">Morning of Travel</h2>
          <p className="text-sm text-muted-foreground">
            {totalMorning} item{totalMorning !== 1 ? "s" : ""} for departure day
          </p>
        </div>
      </div>

      {/* Snoozed items grouped by member */}
      {memberSnoozed.map(({ member, items }) => (
        <div key={member.id} className="overflow-hidden rounded-2xl bg-card md3-elevation-1">
          <div className="flex items-center gap-3 px-4 py-3">
            <Image
              src={member.photo}
              alt={member.name}
              width={32}
              height={32}
              className="h-8 w-8 rounded-full object-cover"
            />
            <span className="flex-1 text-[13px] font-semibold text-foreground">{member.name}</span>
            <span className="rounded-full bg-warning/10 px-2 py-0.5 text-[11px] font-medium text-warning-foreground">
              {items.length} item{items.length !== 1 ? "s" : ""}
            </span>
          </div>
          <div className="mx-4 h-px bg-border/60" />
          <div className="px-1 py-1">
            {items.map((item) => (
              <PackingItem
                key={item.id}
                item={{ ...item, status: "snoozed" }}
                onToggleDone={(id) => onToggleDone(id, member.id)}
                onUnsnooze={(id) => onUnsnooze(id, member.id)}
                showCategory
              />
            ))}
          </div>
        </div>
      ))}

      {/* Morning-only to-do items */}
      <div className="overflow-hidden rounded-2xl bg-card md3-elevation-1">
        <div className="flex items-center gap-3 px-4 py-3">
          <div className="flex h-8 w-8 items-center justify-center rounded-full bg-warning/15">
            <Sunrise className="h-4 w-4 text-warning-foreground" />
          </div>
          <span className="flex-1 text-[13px] font-semibold text-foreground">Before Leaving</span>
          <span className="text-[11px] text-muted-foreground">
            {morningTodo.length} remaining
          </span>
        </div>
        <div className="mx-4 h-px bg-border/60" />
        <div className="px-1 py-1">
          {morningTodo.map((item) => (
            <PackingItem
              key={item.id}
              item={item}
              onToggleDone={onToggleMorningOnlyDone}
              onEdit={onEditMorningOnly}
              onDelete={onDeleteMorningOnly}
            />
          ))}
          {morningDone.map((item) => (
            <PackingItem
              key={item.id}
              item={item}
              onToggleDone={onToggleMorningOnlyDone}
              onEdit={onEditMorningOnly}
              onDelete={onDeleteMorningOnly}
            />
          ))}
          {morningTodo.length === 0 && morningDone.length === 0 && (
            <p className="px-4 py-4 text-center text-xs text-muted-foreground">
              No to-do items yet
            </p>
          )}
        </div>
      </div>

      {/* Add morning item */}
      <AddItemForm
        onAdd={onAddMorningItem}
        categories={["General"]}
        placeholder="e.g. Empty fridge, Lock up..."
      />

      {memberSnoozed.length === 0 && morningOnlyItems.length === 0 && (
        <div className="flex flex-col items-center gap-3 rounded-2xl bg-card py-12 text-center md3-elevation-1">
          <Sunrise className="h-12 w-12 text-muted-foreground/20" />
          <div>
            <p className="text-sm font-medium text-muted-foreground">No morning items yet</p>
            <p className="mt-1 text-xs text-muted-foreground/60">
              Swipe items left to snooze them here
            </p>
          </div>
        </div>
      )}
    </div>
  )
}
