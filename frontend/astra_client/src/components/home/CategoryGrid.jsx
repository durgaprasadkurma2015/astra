import { Smartphone, Shirt, Home, Sparkles, Trophy, Gamepad2, BookOpen, HeartPulse, Car, ShoppingCart } from "lucide-react";

const categories = [
  ["Electronics", Smartphone],
  ["Fashion", Shirt],
  ["Home & Kitchen", Home],
  ["Beauty", Sparkles],
  ["Sports", Trophy],
  ["Toys", Gamepad2],
  ["Books", BookOpen],
  ["Health", HeartPulse],
  ["Automotive", Car],
  ["Grocery", ShoppingCart],
];

export default function CategoryGrid() {
  return (
    <section id="categories" className="category-strip">
      {categories.map(([name, Icon]) => (
        <button className="category-item" key={name}>
          <span><Icon size={24} /></span>
          <b>{name}</b>
        </button>
      ))}
    </section>
  );
}
