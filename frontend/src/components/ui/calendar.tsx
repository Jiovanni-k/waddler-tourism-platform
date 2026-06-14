"use client"

import * as React from "react"
import {
  DayPicker,
  getDefaultClassNames,
  type DayButton,
  type Locale,
} from "react-day-picker"

import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"
import { ChevronLeftIcon, ChevronRightIcon, ChevronDownIcon } from "lucide-react"

function Calendar({
  className,
  classNames,
  showOutsideDays = true,
  captionLayout = "label",
  buttonVariant = "ghost",
  locale,
  formatters,
  components,
  ...props
}: React.ComponentProps<typeof DayPicker> & {
  buttonVariant?: "ghost" | "outline" | "default"
}) {
  const defaultClassNames = getDefaultClassNames()

  return (
    <DayPicker
      showOutsideDays={showOutsideDays}
      className={cn(
        "group/calendar",
        "[--cell-radius:10px] [--cell-size:2.75rem]",
        String.raw`rtl:**:[.rdp-button\_next>svg]:rotate-180`,
        String.raw`rtl:**:[.rdp-button\_previous>svg]:rotate-180`,
        className
      )}
      captionLayout={captionLayout}
      locale={locale}
      formatters={{
        formatMonthDropdown: (date) =>
          date.toLocaleString(locale?.code, { month: "short" }),
        ...formatters,
      }}
      classNames={{
        root:   cn("w-fit", defaultClassNames.root),
        months: cn("relative flex flex-col gap-4 md:flex-row", defaultClassNames.months),
        month:  cn("flex w-full flex-col", defaultClassNames.month),

        /* ── Purple header row ─── */
        nav: cn(
          "absolute inset-x-0 top-0 z-10 flex h-12 w-full items-center justify-between px-1 pointer-events-none",
          defaultClassNames.nav
        ),
        button_previous: cn(
          "size-9 p-0 inline-flex items-center justify-center rounded-xl pointer-events-auto",
          "text-white/70 hover:text-white hover:bg-white/20",
          "focus-visible:outline-none transition-colors select-none",
          defaultClassNames.button_previous
        ),
        button_next: cn(
          "size-9 p-0 inline-flex items-center justify-center rounded-xl pointer-events-auto",
          "text-white/70 hover:text-white hover:bg-white/20",
          "focus-visible:outline-none transition-colors select-none",
          defaultClassNames.button_next
        ),
        month_caption: cn(
          "flex h-12 w-full items-center justify-center px-12 bg-primary",
          defaultClassNames.month_caption
        ),
        caption_label: cn(
          "text-sm font-extrabold text-white select-none tracking-wide",
          captionLayout !== "label" &&
            "flex items-center gap-1 [&>svg]:size-3.5 [&>svg]:text-white/70",
          defaultClassNames.caption_label
        ),
        dropdowns: cn(
          "relative z-20 flex h-12 w-full items-center justify-center gap-1.5 text-sm font-bold text-white",
          defaultClassNames.dropdowns
        ),
        dropdown_root: cn("relative rounded-xl cursor-pointer", defaultClassNames.dropdown_root),
        dropdown:      cn("absolute inset-0 opacity-0 cursor-pointer", defaultClassNames.dropdown),

        /* ── Weekday row ─── */
        weekdays: cn("flex border-b border-primary/10 bg-primary/8", defaultClassNames.weekdays),
        weekday:  cn(
          "flex-1 py-2 text-[11px] font-bold text-primary/60 uppercase tracking-wider text-center select-none",
          defaultClassNames.weekday
        ),

        /* ── Day grid ─── */
        table:       "w-full border-collapse",
        week_number_header: cn("w-(--cell-size) select-none", defaultClassNames.week_number_header),
        week_number: cn("text-xs text-muted-foreground select-none", defaultClassNames.week_number),
        week:        cn("mt-1 flex w-full gap-0.5 px-1", defaultClassNames.week),
        day: cn(
          "group/day relative aspect-square h-full w-full p-0 text-center select-none",
          "[&:last-child[data-selected=true]_button]:rounded-r-[--cell-radius]",
          props.showWeekNumber
            ? "[&:nth-child(2)[data-selected=true]_button]:rounded-l-[--cell-radius]"
            : "[&:first-child[data-selected=true]_button]:rounded-l-[--cell-radius]",
          defaultClassNames.day
        ),

        /* ── Range states ─── */
        range_start: cn(
          "relative isolate z-0 rounded-l-[--cell-radius] bg-primary/12",
          "after:absolute after:inset-y-0 after:right-0 after:w-3 after:bg-primary/12",
          defaultClassNames.range_start
        ),
        range_middle: cn("rounded-none bg-primary/12", defaultClassNames.range_middle),
        range_end:    cn(
          "relative isolate z-0 rounded-r-[--cell-radius] bg-primary/12",
          "after:absolute after:inset-y-0 after:left-0 after:w-3 after:bg-primary/12",
          defaultClassNames.range_end
        ),

        /* ── Day states ─── */
        today:    cn("rounded-[--cell-radius] font-extrabold text-primary data-[selected=true]:text-primary-foreground", defaultClassNames.today),
        outside:  cn("text-muted-foreground/30 aria-selected:text-muted-foreground", defaultClassNames.outside),
        disabled: cn("text-muted-foreground/25 opacity-40", defaultClassNames.disabled),
        hidden:   cn("invisible", defaultClassNames.hidden),

        ...classNames,
      }}
      components={{
        Root: ({ className, rootRef, ...props }) => (
          <div
            data-slot="calendar"
            ref={rootRef}
            className={cn("overflow-hidden", className)}
            {...props}
          />
        ),
        Chevron: ({ className, orientation, ...props }) => {
          if (orientation === "left")
            return <ChevronLeftIcon className={cn("size-4", className)} {...props} />
          if (orientation === "right")
            return <ChevronRightIcon className={cn("size-4", className)} {...props} />
          return <ChevronDownIcon className={cn("size-4", className)} {...props} />
        },
        DayButton: ({ ...props }) => (
          <CalendarDayButton locale={locale} {...props} />
        ),
        WeekNumber: ({ children, ...props }) => (
          <td {...props}>
            <div className="flex size-(--cell-size) items-center justify-center text-center">
              {children}
            </div>
          </td>
        ),
        ...components,
      }}
      {...props}
    />
  )
}

function CalendarDayButton({
  className,
  day,
  modifiers,
  locale,
  ...props
}: React.ComponentProps<typeof DayButton> & { locale?: Partial<Locale> }) {
  const ref = React.useRef<HTMLButtonElement>(null)
  React.useEffect(() => {
    if (modifiers.focused) ref.current?.focus()
  }, [modifiers.focused])

  return (
    <button
      ref={ref}
      data-day={day.date.toLocaleDateString(locale?.code)}
      data-selected-single={
        modifiers.selected && !modifiers.range_start && !modifiers.range_end && !modifiers.range_middle
      }
      data-range-start={modifiers.range_start}
      data-range-end={modifiers.range_end}
      data-range-middle={modifiers.range_middle}
      className={cn(
        // Base layout
        "relative isolate z-10 flex aspect-square size-auto w-full min-w-(--cell-size)",
        "items-center justify-center rounded-[--cell-radius]",
        "text-sm font-medium leading-none border-0 bg-transparent cursor-pointer",
        "transition-colors duration-100 focus-visible:outline-none",
        // Hover
        "hover:bg-primary/12 hover:text-primary",
        // Range states
        "data-[range-start=true]:rounded-l-[--cell-radius] data-[range-start=true]:rounded-r-none",
        "data-[range-start=true]:bg-primary data-[range-start=true]:text-white",
        "data-[range-end=true]:rounded-r-[--cell-radius] data-[range-end=true]:rounded-l-none",
        "data-[range-end=true]:bg-primary data-[range-end=true]:text-white",
        "data-[range-middle=true]:rounded-none data-[range-middle=true]:bg-primary/12",
        // Selected single
        "data-[selected-single=true]:bg-primary data-[selected-single=true]:text-white",
        "data-[selected-single=true]:shadow-sm data-[selected-single=true]:font-bold",
        "data-[selected-single=true]:hover:bg-primary/90",
        className
      )}
      {...props}
    />
  )
}

export { Calendar, CalendarDayButton }
