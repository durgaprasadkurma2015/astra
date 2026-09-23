import { Routes, Route } from "react-router-dom";
import Layout from "./components/Layout";
import ProtectedRoute from "./components/ProtectedRoute";
import Home from "./pages/Home";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Products from "./pages/Products";
import ProductDetails from "./pages/ProductDetails";
import Cart from "./pages/Cart";
import Checkout from "./pages/Checkout";
import Orders from "./pages/Orders";
import OrderDetails from "./pages/OrderDetails";
import Profile from "./pages/Profile";
import Wishlist from "./pages/Wishlist";
import ForgotPassword from "./pages/ForgotPassword";
import AdminDashboard from "./pages/AdminDashboard";
import SellerDashboard from "./pages/SellerDashboard";
import NotFound from "./pages/NotFound";

export default function App() {
  return <Layout>
    <Routes>
      <Route path="/" element={<Home/>}/>
      <Route path="/login" element={<Login/>}/>
      <Route path="/register" element={<Register/>}/>
      <Route path="/forgot-password" element={<ForgotPassword/>}/>
      <Route path="/products" element={<Products/>}/>
      <Route path="/products/:id" element={<ProductDetails/>}/>
      <Route path="/cart" element={<Cart/>}/>
      <Route path="/wishlist" element={<Wishlist/>}/>
      <Route element={<ProtectedRoute/>}>
        <Route path="/checkout" element={<Checkout/>}/>
        <Route path="/orders" element={<Orders/>}/>
        <Route path="/orders/:id" element={<OrderDetails/>}/>
        <Route path="/profile" element={<Profile/>}/>
        <Route path="/shipments" element={<NotFound/>}/>
      </Route>
      <Route element={<ProtectedRoute roles={["ADMIN"]}/>}>
        <Route path="/admin" element={<AdminDashboard/>}/>
      </Route>
      <Route path="/search" element={<Products />} />
<Route path="/search" element={<Products />} />
<Route path="/products" element={<Products />} />
<Route path="/search" element={<Products />} />
<Route path="/products/:id" element={<ProductDetails />} />

      <Route element={<ProtectedRoute roles={["SELLER","ADMIN"]}/>}>
        <Route path="/seller" element={<SellerDashboard/>}/>
      </Route>
      <Route path="*" element={<NotFound/>}/>
    </Routes>
  </Layout>;
}