"use client"

import { useRef, useState, useCallback, useEffect } from "react"
import { Check, Sunrise, Undo2, Pencil, Trash2, X } from "lucide-react"
import { cn } from "@/lib/utils"
import type { PackingItem as PackingItemType } from "@/lib/packing-data"

interface PackingItemProps {
  item: PackingItemType
  onToggleDone: (id: string) => void
  onSnooze?: (id: string) => void
  onUnsnooze?: (id: string) => void
  onEdit?: (id: string, newName: string) => void
  onDelete?: (id: string) => void
  showCategory?: boolean
  showOwner?: string
}

const SWIPE_THRESHOLD = 72

export function PackingItem({
  item,
  onToggleDone,
  onSnooze,
  onUnsnooze,
  onEdit,
  onDelete,
  showCategory = false,
  showOwner,
}: PackingItemProps) {
  const isDone = item.status === "done"
  const isSnoozed = item.status === "snoozed"

  const [offsetX, setOffsetX] = useState(0)
  const [dismissDirection, setDismissDirection] = useState<"snooze" | "done" | null>(null)
  const [isEditing, setIsEditing] = useState(false)
  const [editValue, setEditValue] = useState(item.name)
  const [confirmDelete, setConfirmDelete] = useState(false)

  const editInputRef = useRef<HTMLInputElement>(null)
  const rowRef = useRef<HTMLDivElement>(null)
  const startXRef = useRef(0)
  const startYRef = useRef(0)
  const isTrackingRef = useRef(false)
  const isHorizontalRef = useRef<boolean | null>(null)
  const hasMoveRef = useRef(false)
  const deleteTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null)

  const canSwipe = !isDone && !isSnoozed && !!onSnooze && !isEditing

  useEffect(() => {
    return () => {
      if (deleteTimerRef.current) clearTimeout(deleteTimerRef.current)
    }
  }, [])

  // --- Swipe handlers ---
  const handleStart = useCallback(
    (clientX: number, clientY: number) => {
      if (!canSwipe) return
      startXRef.current = clientX
      startYRef.current = clientY
      isTrackingRef.current = true
      isHorizontalRef.current = null
      hasMoveRef.current = false
    },
    [canSwipe]
  )

  const handleMove = useCallback(
    (clientX: number, clientY: number) => {
      if (!isTrackingRef.current || !canSwipe) return
      const diffX = startXRef.current - clientX
      const diffY = clientY - startYRef.current

      if (isHorizontalRef.current === null && (Math.abs(diffX) > 8 || Math.abs(diffY) > 8)) {
        isHorizontalRef.current = Math.abs(diffX) > Math.abs(diffY)
        if (!isHorizontalRef.current) {
          isTrackingRef.current = false
          return
        }
      }
      if (!isHorizontalRef.current) return
      hasMoveRef.current = true
      setOffsetX(Math.max(0, Math.min(diffX, 140)))
    },
    [canSwipe]
  )

  const handleEnd = useCallback(() => {
    if (!isTrackingRef.current) return
    isTrackingRef.current = false
    isHorizontalRef.current = null

    if (offsetX > SWIPE_THRESHOLD) {
      setDismissDirection("snooze")
    } else {
      setOffsetX(0)
    }
  }, [offsetX])

  // Touch
  const onTouchStart = useCallback(
    (e: React.TouchEvent) => handleStart(e.touches[0].clientX, e.touches[0].clientY),
    [handleStart]
  )
  const onTouchMove = useCallback(
    (e: React.TouchEvent) => handleMove(e.touches[0].clientX, e.touches[0].clientY),
    [handleMove]
  )
  const onTouchEnd = useCallback(() => handleEnd(), [handleEnd])

  // Mouse
  const onMouseDown = useCallback(
    (e: React.MouseEvent) => {
      if (isEditing) return
      handleStart(e.clientX, e.clientY)
      const onMM = (ev: MouseEvent) => handleMove(ev.clientX, ev.clientY)
      const onMU = () => {
        handleEnd()
        window.removeEventListener("mousemove", onMM)
        window.removeEventListener("mouseup", onMU)
      }
      window.addEventListener("mousemove", onMM)
      window.addEventListener("mouseup", onMU)
    },
    [handleStart, handleMove, handleEnd, isEditing]
  )

  const isThresholdMet = offsetX >= SWIPE_THRESHOLD

  // --- Done animation ---
  const handleToggleDone = useCallback(() => {
    if (hasMoveRef.current) return
    if (!isDone) {
      setDismissDirection("done")
    } else {
      onToggleDone(item.id)
    }
  }, [isDone, onToggleDone, item.id])

  // When dismiss animation ends
  const onAnimationEnd = useCallback(() => {
    if (dismissDirection === "snooze") {
      onSnooze?.(item.id)
    } else if (dismissDirection === "done") {
      onToggleDone(item.id)
    }
    setDismissDirection(null)
    setOffsetX(0)
  }, [dismissDirection, item.id, onSnooze, onToggleDone])

  // --- Edit ---
  const handleSaveEdit = () => {
    const trimmed = editValue.trim()
    if (trimmed && trimmed !== item.name) {
      onEdit?.(item.id, trimmed)
    }
    setIsEditing(false)
  }

  // --- Delete ---
  const handleDeleteClick = () => {
    if (confirmDelete) {
      if (deleteTimerRef.current) clearTimeout(deleteTimerRef.current)
      onDelete?.(item.id)
    } else {
      setConfirmDelete(true)
      deleteTimerRef.current = setTimeout(() => setConfirmDelete(false), 3000)
    }
  }

  return (
    <div
      ref={rowRef}
      className={cn("relative overflow-hidden", dismissDirection && "animate-md3-dismiss")}
      onAnimationEnd={onAnimationEnd}
    >
      {/* Swipe reveal background */}
      {canSwipe && offsetX > 0 && (
        <div
          className={cn(
            "absolute inset-0 flex items-center justify-end rounded-2xl px-4 transition-colors duration-75",
            isThresholdMet ? "bg-warning/20" : "bg-warning/8"
          )}
        >
          <div className="flex items-center gap-2">
            <Sunrise
              className={cn(
                "h-4 w-4 transition-all duration-75",
                isThresholdMet ? "text-warning-foreground scale-110" : "text-warning-foreground/40"
              )}
            />
            <span
              className={cn(
                "text-xs font-medium transition-all duration-75",
                isThresholdMet ? "text-warning-foreground" : "text-warning-foreground/40"
              )}
            >
              Morning
            </span>
          </div>
        </div>
      )}

      {/* Foreground row */}
      <div
        className={cn(
          "relative flex items-center gap-3 rounded-2xl bg-card px-3 py-3",
          offsetX > 0 ? "transition-none" : "transition-transform duration-200 ease-out",
          isDone && "opacity-50",
          canSwipe && "select-none"
        )}
        style={{ transform: canSwipe && offsetX > 0 ? `translateX(-${offsetX}px)` : undefined }}
        onTouchStart={canSwipe ? onTouchStart : undefined}
        onTouchMove={canSwipe ? onTouchMove : undefined}
        onTouchEnd={canSwipe ? onTouchEnd : undefined}
        onMouseDown={canSwipe ? onMouseDown : undefined}
      >
        {/* Checkbox */}
        <button
          onClick={(e) => {
            e.stopPropagation()
            handleToggleDone()
          }}
          className={cn(
            "relative flex h-[22px] w-[22px] shrink-0 items-center justify-center rounded-[4px] border-2 transition-all duration-200",
            isDone
              ? "border-success bg-success"
              : "border-muted-foreground/40 hover:border-primary"
          )}
          aria-label={isDone ? `Uncheck ${item.name}` : `Check off ${item.name}`}
        >
          {isDone && (
            <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
              <path
                d="M2.5 6.5L5 9L9.5 3.5"
                stroke="white"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
            </svg>
          )}
        </button>

        {/* Content */}
        <div className="flex flex-1 flex-col gap-0.5 overflow-hidden">
          {isEditing ? (
            <div className="flex items-center gap-1.5">
              <input
                ref={editInputRef}
                type="text"
                value={editValue}
                onChange={(e) => setEditValue(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === "Enter") handleSaveEdit()
                  if (e.key === "Escape") {
                    setEditValue(item.name)
                    setIsEditing(false)
                  }
                }}
                className="min-w-0 flex-1 rounded-lg border border-primary/40 bg-background px-2.5 py-1 text-sm text-foreground focus:outline-none focus:ring-2 focus:ring-primary/30"
                autoFocus
              />
              <button
                onClick={handleSaveEdit}
                className="rounded-full p-1.5 text-success hover:bg-success/10"
                aria-label="Save"
              >
                <Check className="h-4 w-4" />
              </button>
              <button
                onClick={() => {
                  setEditValue(item.name)
                  setIsEditing(false)
                }}
                className="rounded-full p-1.5 text-muted-foreground hover:bg-muted"
                aria-label="Cancel"
              >
                <X className="h-4 w-4" />
              </button>
            </div>
          ) : (
            <>
              <span
                className={cn(
                  "truncate text-sm leading-snug transition-all duration-200",
                  isDone
                    ? "text-muted-foreground line-through decoration-muted-foreground/40"
                    : "text-foreground"
                )}
              >
                {item.name}
              </span>
              {(showCategory || showOwner) && (
                <span className="truncate text-[11px] leading-tight text-muted-foreground/70">
                  {showOwner ? showOwner : ""}
                  {showOwner && showCategory ? " \u00B7 " : ""}
                  {showCategory ? item.category : ""}
                </span>
              )}
            </>
          )}
        </div>

        {/* Actions - only show if not swiping */}
        {!isEditing && offsetX === 0 && (
          <div className="flex items-center gap-0.5">
            {isSnoozed && onUnsnooze && (
              <button
                onClick={() => onUnsnooze(item.id)}
                className="flex items-center gap-1 rounded-full px-2.5 py-1 text-xs font-medium text-primary transition-colors hover:bg-primary/10"
                aria-label={`Move ${item.name} back`}
              >
                <Undo2 className="h-3 w-3" />
                <span>Back</span>
              </button>
            )}
            {onEdit && (
              <button
                onClick={() => {
                  setEditValue(item.name)
                  setIsEditing(true)
                  setTimeout(() => editInputRef.current?.focus(), 50)
                }}
                className="rounded-full p-2 text-muted-foreground/40 transition-colors hover:bg-muted hover:text-muted-foreground"
                aria-label={`Edit ${item.name}`}
              >
                <Pencil className="h-3.5 w-3.5" />
              </button>
            )}
            {onDelete && (
              <button
                onClick={handleDeleteClick}
                className={cn(
                  "rounded-full p-2 transition-colors",
                  confirmDelete
                    ? "bg-destructive/10 text-destructive"
                    : "text-muted-foreground/40 hover:bg-destructive/10 hover:text-destructive"
                )}
                aria-label={
                  confirmDelete ? `Confirm delete ${item.name}` : `Delete ${item.name}`
                }
              >
                <Trash2 className="h-3.5 w-3.5" />
              </button>
            )}
          </div>
        )}
      </div>
    </div>
  )
}
