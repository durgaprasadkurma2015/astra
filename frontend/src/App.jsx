
import AppRoutes from "./routes/AppRoutes";
import { WishlistProvider } from "./context/WishlistContext.jsx";
import { CompareProvider } from "./context/CompareContext.jsx";

export default function App() {
  return (
    <WishlistProvider>
      <CompareProvider>
        <AppRoutes />
      </CompareProvider>
    </WishlistProvider>
  );
}
