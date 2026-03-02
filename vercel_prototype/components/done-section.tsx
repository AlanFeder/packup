"use client"

import { useState } from "react"
import { CheckCircle2, ChevronDown, ChevronRight } from "lucide-react"
import type { PackingItem as PackingItemType } from "@/lib/packing-data"
import { PackingItem } from "./packing-item"

interface DoneSectionProps {
  items: PackingItemType[]
  onToggleDone: (id: string) => void
  onEdit: (id: string, newName: string) => void
  onDelete: (id: string) => void
}

export function DoneSection({ items, onToggleDone, onEdit, onDelete }: DoneSectionProps) {
  const [isExpanded, setIsExpanded] = useState(false)

  if (items.length === 0) return null

  return (
    <section className="mt-4" aria-label="Packed items">
      <button
        onClick={() => setIsExpanded(!isExpanded)}
        className="mb-2 flex w-full items-center gap-3 rounded-2xl px-4 py-3 text-left transition-colors hover:bg-success/5 active:bg-success/10"
      >
        <div className="flex h-8 w-8 items-center justify-center rounded-full bg-success/10">
          {isExpanded ? (
            <ChevronDown className="h-4 w-4 text-success" />
          ) : (
            <ChevronRight className="h-4 w-4 text-success" />
          )}
        </div>
        <div className="flex items-center gap-2">
          <CheckCircle2 className="h-4 w-4 text-success" />
          <span className="text-sm font-medium text-muted-foreground">
            Packed ({items.length})
          </span>
        </div>
      </button>

      {isExpanded && (
        <div className="animate-md3-appear overflow-hidden rounded-2xl bg-card/60 px-1 py-1 md3-elevation-1">
          {items.map((item) => (
            <PackingItem
              key={item.id}
              item={item}
              onToggleDone={onToggleDone}
              onEdit={onEdit}
              onDelete={onDelete}
              showCategory
            />
          ))}
        </div>
      )}
    </section>
  )
}
