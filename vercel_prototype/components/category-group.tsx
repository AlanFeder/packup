"use client"

import {
  Shirt, Droplets, Zap, FileText, Sun, Watch,
  HeartPulse, Gamepad2, Cookie, Package, BedDouble,
  Wrench, ToyBrick, ShieldCheck, CheckCircle2, CheckCheck,
  type LucideIcon,
} from "lucide-react"
import { cn } from "@/lib/utils"
import { CATEGORY_COLORS, CATEGORY_ICONS } from "@/lib/packing-data"
import type { PackingItem as PackingItemType } from "@/lib/packing-data"
import { PackingItem } from "./packing-item"

export const ICON_MAP: Record<string, LucideIcon> = {
  shirt: Shirt,
  droplets: Droplets,
  zap: Zap,
  "file-text": FileText,
  sun: Sun,
  watch: Watch,
  "heart-pulse": HeartPulse,
  "gamepad-2": Gamepad2,
  cookie: Cookie,
  "bed-double": BedDouble,
  wrench: Wrench,
  "toy-brick": ToyBrick,
  "shield-check": ShieldCheck,
  package: Package,
}

interface CategoryGroupProps {
  category: string
  items: PackingItemType[]
  onToggleDone: (id: string) => void
  onSnooze: (id: string) => void
  onEdit: (id: string, newName: string) => void
  onDelete: (id: string) => void
  onMarkAllDone: (category: string) => void
}

export function CategoryGroup({
  category,
  items,
  onToggleDone,
  onSnooze,
  onEdit,
  onDelete,
  onMarkAllDone,
}: CategoryGroupProps) {
  const iconKey = CATEGORY_ICONS[category] || "package"
  const Icon = ICON_MAP[iconKey] || Package
  const colorClasses = CATEGORY_COLORS[category] || "bg-muted text-muted-foreground"

  const todoItems = items.filter((i) => i.status === "todo")
  const totalForCategory = items.length
  const doneForCategory = items.filter((i) => i.status === "done").length
  const snoozedForCategory = items.filter((i) => i.status === "snoozed").length
  const handledCount = doneForCategory + snoozedForCategory

  if (todoItems.length === 0 && totalForCategory > 0) {
    return (
      <div className="flex items-center gap-3 rounded-2xl bg-success/5 px-4 py-3">
        <div className="flex h-8 w-8 items-center justify-center rounded-full bg-success/10">
          <CheckCircle2 className="h-4 w-4 text-success" />
        </div>
        <span className="flex-1 text-sm font-medium text-foreground/60">{category}</span>
        <span className="rounded-full bg-success/10 px-2 py-0.5 text-[11px] font-medium text-success">
          All packed
        </span>
      </div>
    )
  }

  return (
    <div className="overflow-hidden rounded-2xl bg-card md3-elevation-1">
      {/* Header */}
      <div className="flex items-center gap-3 px-4 py-3">
        <div className={cn("flex h-8 w-8 items-center justify-center rounded-full", colorClasses)}>
          <Icon className="h-4 w-4" />
        </div>
        <h3 className="flex-1 text-[13px] font-semibold tracking-wide text-foreground">
          {category}
        </h3>
        {todoItems.length > 0 && (
          <button
            onClick={() => onMarkAllDone(category)}
            className="flex items-center gap-1 rounded-full px-2 py-1 text-[11px] font-medium text-primary transition-colors hover:bg-primary/10 active:scale-95"
            aria-label={`Mark all ${category} items as done`}
          >
            <CheckCheck className="h-3 w-3" />
            All done
          </button>
        )}
        <span className="text-[11px] font-medium text-muted-foreground tabular-nums">
          {handledCount}/{totalForCategory}
        </span>
        {totalForCategory > 0 && (
          <div className="h-1 w-12 overflow-hidden rounded-full bg-muted">
            <div
              className="h-full rounded-full bg-primary transition-all duration-500 ease-out"
              style={{ width: `${(handledCount / totalForCategory) * 100}%` }}
            />
          </div>
        )}
      </div>

      {/* Divider */}
      <div className="mx-4 h-px bg-border/60" />

      {/* Items */}
      <div className="px-1 py-1">
        {todoItems.map((item) => (
          <PackingItem
            key={item.id}
            item={item}
            onToggleDone={onToggleDone}
            onSnooze={onSnooze}
            onEdit={onEdit}
            onDelete={onDelete}
          />
        ))}
      </div>
    </div>
  )
}
