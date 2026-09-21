import { ChevronRight, Smartphone, Shirt, Home, Sparkles, Trophy, Gamepad2, BookOpen, HeartPulse, Car, ShoppingBasket } from "lucide-react";

const items = [
  ["Electronics", Smartphone],
  ["Fashion", Shirt],
  ["Home & Kitchen", Home],
  ["Beauty & Personal Care", Sparkles],
  ["Sports & Outdoors", Trophy],
  ["Toys & Games", Gamepad2],
  ["Books", BookOpen],
  ["Health & Wellness", HeartPulse],
  ["Automotive", Car],
  ["Grocery & Gourmet", ShoppingBasket],
];

export default function SidebarCategories() {
  return (
    <aside className="sidebar-categories">
      {items.map(([label, Icon]) => (
        <button key={label}>
          <Icon size={17} />
          <span>{label}</span>
          <ChevronRight size={15} />
        </button>
      ))}
    </aside>
  );
}
