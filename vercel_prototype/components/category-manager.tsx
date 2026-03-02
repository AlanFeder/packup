"use client"

import { useState, useRef } from "react"
import {
  Plus, X, Pencil, Check, Tag,
  Shirt, Droplets, Zap, FileText, Sun, Watch,
  HeartPulse, Gamepad2, Cookie, Package, BedDouble,
  Wrench, ToyBrick, ShieldCheck, Briefcase, Plane,
  Camera, Umbrella, type LucideIcon,
} from "lucide-react"
import { cn } from "@/lib/utils"
import { CATEGORY_ICONS, CATEGORY_COLORS } from "@/lib/packing-data"

const AVAILABLE_ICONS: { key: string; icon: LucideIcon; label: string }[] = [
  { key: "package", icon: Package, label: "Box" },
  { key: "shirt", icon: Shirt, label: "Shirt" },
  { key: "droplets", icon: Droplets, label: "Drops" },
  { key: "zap", icon: Zap, label: "Zap" },
  { key: "file-text", icon: FileText, label: "Doc" },
  { key: "sun", icon: Sun, label: "Sun" },
  { key: "watch", icon: Watch, label: "Watch" },
  { key: "heart-pulse", icon: HeartPulse, label: "Health" },
  { key: "gamepad-2", icon: Gamepad2, label: "Game" },
  { key: "cookie", icon: Cookie, label: "Food" },
  { key: "bed-double", icon: BedDouble, label: "Bed" },
  { key: "wrench", icon: Wrench, label: "Tool" },
  { key: "toy-brick", icon: ToyBrick, label: "Toy" },
  { key: "shield-check", icon: ShieldCheck, label: "Safety" },
  { key: "briefcase", icon: Briefcase, label: "Case" },
  { key: "plane", icon: Plane, label: "Travel" },
  { key: "camera", icon: Camera, label: "Camera" },
  { key: "umbrella", icon: Umbrella, label: "Umbrella" },
]

interface CategoryManagerProps {
  categories: string[]
  onAddCategory: (name: string, iconKey: string) => void
  onRenameCategory: (oldName: string, newName: string) => void
  onDeleteCategory: (name: string) => void
  onSetCategoryIcon: (name: string, iconKey: string) => void
}

export function CategoryManager({
  categories,
  onAddCategory,
  onRenameCategory,
  onDeleteCategory,
  onSetCategoryIcon,
}: CategoryManagerProps) {
  const [isOpen, setIsOpen] = useState(false)
  const [newCat, setNewCat] = useState("")
  const [newIcon, setNewIcon] = useState("package")
  const [editingCat, setEditingCat] = useState<string | null>(null)
  const [editValue, setEditValue] = useState("")
  const [pickingIconFor, setPickingIconFor] = useState<string | null>(null)
  const [confirmDelete, setConfirmDelete] = useState<string | null>(null)
  const inputRef = useRef<HTMLInputElement>(null)

  const handleAdd = (e: React.FormEvent) => {
    e.preventDefault()
    const trimmed = newCat.trim()
    if (!trimmed || categories.includes(trimmed)) return
    onAddCategory(trimmed, newIcon)
    setNewCat("")
    setNewIcon("package")
    inputRef.current?.focus()
  }

  const handleRename = (oldName: string) => {
    const trimmed = editValue.trim()
    if (trimmed && trimmed !== oldName && !categories.includes(trimmed)) {
      onRenameCategory(oldName, trimmed)
    }
    setEditingCat(null)
    setEditValue("")
  }

  const handleDelete = (cat: string) => {
    if (confirmDelete === cat) {
      onDeleteCategory(cat)
      setConfirmDelete(null)
    } else {
      setConfirmDelete(cat)
      setTimeout(() => setConfirmDelete(null), 3000)
    }
  }

  const getIconForCategory = (cat: string) => {
    const key = CATEGORY_ICONS[cat] || "package"
    return AVAILABLE_ICONS.find((i) => i.key === key)?.icon || Package
  }

  if (!isOpen) {
    return (
      <button
        onClick={() => setIsOpen(true)}
        className="flex items-center gap-2 rounded-xl px-3 py-2 text-xs font-medium text-muted-foreground transition-colors hover:bg-muted hover:text-foreground"
      >
        <Tag className="h-3.5 w-3.5" />
        Manage categories
      </button>
    )
  }

  return (
    <div className="overflow-hidden rounded-2xl bg-card md3-elevation-1">
      <div className="flex items-center justify-between border-b border-border/60 px-4 py-3">
        <div className="flex items-center gap-2">
          <Tag className="h-4 w-4 text-primary" />
          <h3 className="text-sm font-semibold text-foreground">Categories</h3>
        </div>
        <button
          onClick={() => { setIsOpen(false); setPickingIconFor(null) }}
          className="rounded-full p-1.5 text-muted-foreground hover:bg-muted"
          aria-label="Close category manager"
        >
          <X className="h-4 w-4" />
        </button>
      </div>

      <div className="max-h-72 overflow-y-auto px-2 py-1">
        {categories.map((cat) => {
          const CatIcon = getIconForCategory(cat)
          const colorCls = CATEGORY_COLORS[cat] || "bg-muted text-muted-foreground"

          return (
            <div key={cat}>
              <div className="flex items-center gap-2 rounded-xl px-2 py-2">
                {editingCat === cat ? (
                  <>
                    <input
                      type="text"
                      value={editValue}
                      onChange={(e) => setEditValue(e.target.value)}
                      onKeyDown={(e) => {
                        if (e.key === "Enter") handleRename(cat)
                        if (e.key === "Escape") {
                          setEditingCat(null)
                          setEditValue("")
                        }
                      }}
                      className="min-w-0 flex-1 rounded-lg bg-muted/50 px-2.5 py-1 text-sm text-foreground focus:outline-none focus:ring-2 focus:ring-primary/30"
                      autoFocus
                    />
                    <button
                      onClick={() => handleRename(cat)}
                      className="rounded-full p-1.5 text-success hover:bg-success/10"
                      aria-label="Save"
                    >
                      <Check className="h-3.5 w-3.5" />
                    </button>
                    <button
                      onClick={() => {
                        setEditingCat(null)
                        setEditValue("")
                      }}
                      className="rounded-full p-1.5 text-muted-foreground hover:bg-muted"
                      aria-label="Cancel"
                    >
                      <X className="h-3.5 w-3.5" />
                    </button>
                  </>
                ) : (
                  <>
                    {/* Icon button to open icon picker */}
                    <button
                      onClick={() =>
                        setPickingIconFor(pickingIconFor === cat ? null : cat)
                      }
                      className={cn(
                        "flex h-7 w-7 shrink-0 items-center justify-center rounded-full transition-colors",
                        pickingIconFor === cat
                          ? "ring-2 ring-primary ring-offset-1 ring-offset-card"
                          : "",
                        colorCls
                      )}
                      aria-label={`Change icon for ${cat}`}
                    >
                      <CatIcon className="h-3.5 w-3.5" />
                    </button>
                    <span className="flex-1 text-sm text-foreground">{cat}</span>
                    <button
                      onClick={() => {
                        setEditingCat(cat)
                        setEditValue(cat)
                      }}
                      className="rounded-full p-1.5 text-muted-foreground/40 hover:bg-muted hover:text-muted-foreground"
                      aria-label={`Rename ${cat}`}
                    >
                      <Pencil className="h-3 w-3" />
                    </button>
                    <button
                      onClick={() => handleDelete(cat)}
                      className={cn(
                        "rounded-full p-1.5 transition-colors",
                        confirmDelete === cat
                          ? "bg-destructive/10 text-destructive"
                          : "text-muted-foreground/40 hover:bg-destructive/10 hover:text-destructive"
                      )}
                      aria-label={
                        confirmDelete === cat
                          ? `Confirm delete ${cat}`
                          : `Delete ${cat}`
                      }
                    >
                      <X className="h-3 w-3" />
                    </button>
                  </>
                )}
              </div>

              {/* Icon picker row */}
              {pickingIconFor === cat && (
                <div className="mb-1 flex flex-wrap gap-1.5 px-3 pb-2 animate-md3-appear">
                  {AVAILABLE_ICONS.map(({ key, icon: Ic }) => {
                    const isSelected = (CATEGORY_ICONS[cat] || "package") === key
                    return (
                      <button
                        key={key}
                        onClick={() => {
                          onSetCategoryIcon(cat, key)
                          setPickingIconFor(null)
                        }}
                        className={cn(
                          "flex h-8 w-8 items-center justify-center rounded-full transition-all",
                          isSelected
                            ? "bg-primary text-primary-foreground"
                            : "bg-muted/60 text-muted-foreground hover:bg-primary/10 hover:text-primary"
                        )}
                        aria-label={key}
                      >
                        <Ic className="h-3.5 w-3.5" />
                      </button>
                    )
                  })}
                </div>
              )}
            </div>
          )
        })}
      </div>

      {/* Add new category with icon chooser */}
      <form
        onSubmit={handleAdd}
        className="flex items-center gap-2 border-t border-border/60 px-3 py-2.5"
      >
        {/* Icon chooser for new category */}
        <div className="relative">
          <button
            type="button"
            onClick={() =>
              setPickingIconFor(pickingIconFor === "__new__" ? null : "__new__")
            }
            className={cn(
              "flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-muted/60 text-muted-foreground transition-colors hover:bg-primary/10",
              pickingIconFor === "__new__" &&
                "ring-2 ring-primary ring-offset-1 ring-offset-card"
            )}
            aria-label="Choose icon for new category"
          >
            {(() => {
              const Ic =
                AVAILABLE_ICONS.find((i) => i.key === newIcon)?.icon || Package
              return <Ic className="h-3.5 w-3.5" />
            })()}
          </button>
        </div>
        <input
          ref={inputRef}
          type="text"
          value={newCat}
          onChange={(e) => setNewCat(e.target.value)}
          placeholder="New category..."
          className="min-w-0 flex-1 rounded-lg bg-muted/50 px-2.5 py-1.5 text-sm text-foreground placeholder:text-muted-foreground/50 focus:outline-none focus:ring-2 focus:ring-primary/30"
        />
        <button
          type="submit"
          disabled={!newCat.trim() || categories.includes(newCat.trim())}
          className="flex items-center gap-1 rounded-lg bg-primary px-3 py-1.5 text-xs font-medium text-primary-foreground transition-all hover:bg-primary/90 active:scale-95 disabled:opacity-40"
        >
          <Plus className="h-3 w-3" />
          Add
        </button>
      </form>

      {/* New category icon picker */}
      {pickingIconFor === "__new__" && (
        <div className="flex flex-wrap gap-1.5 border-t border-border/60 px-3 py-2.5 animate-md3-appear">
          {AVAILABLE_ICONS.map(({ key, icon: Ic }) => (
            <button
              key={key}
              type="button"
              onClick={() => {
                setNewIcon(key)
                setPickingIconFor(null)
              }}
              className={cn(
                "flex h-8 w-8 items-center justify-center rounded-full transition-all",
                newIcon === key
                  ? "bg-primary text-primary-foreground"
                  : "bg-muted/60 text-muted-foreground hover:bg-primary/10 hover:text-primary"
              )}
              aria-label={key}
            >
              <Ic className="h-3.5 w-3.5" />
            </button>
          ))}
        </div>
      )}
    </div>
  )
}
