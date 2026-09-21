import reducer, { setCart, clearCart } from '../../store/cartSlice';

describe('cartSlice', () => {
  it('stores server cart totals and items', () => {
    const payload = { items: [{ productId: 1, quantity: 2 }], subtotal: 50, shippingFee: 5.99, total: 55.99, itemCount: 2 };
    const state = reducer(undefined, setCart(payload));
    expect(state.items).toHaveLength(1);
    expect(state.subtotal).toBe(50);
    expect(state.total).toBe(55.99);
    expect(state.itemCount).toBe(2);
  });

  it('clears cart state', () => {
    const state = reducer({ items: [{ productId: 1 }], subtotal: 10, shippingFee: 5.99, total: 15.99, itemCount: 1 }, clearCart());
    expect(state).toEqual({ items: [], subtotal: 0, shippingFee: 0, total: 0, itemCount: 0 });
  });
});
