import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { vi } from 'vitest';
import ProductCard from '../../components/product/ProductCard';

const product = { id: 1, name: 'Wireless Headphones', brand: 'ASTRA Audio', image: '/headphones.jpg', price: 49.99, oldPrice: 79.99, rating: 4.5, reviews: 120, discount: 37 };

describe('ProductCard', () => {
  it('renders product information', () => {
    render(<ProductCard product={product} onAdd={() => {}} />);
    expect(screen.getByRole('heading', { name: 'Wireless Headphones' })).toBeInTheDocument();
    expect(screen.getByText('ASTRA Audio')).toBeInTheDocument();
    expect(screen.getByText('37% OFF')).toBeInTheDocument();
    expect(screen.getByRole('img', { name: 'Wireless Headphones' })).toHaveAttribute('src', '/headphones.jpg');
  });

  it('calls onAdd with the product', async () => {
    const user = userEvent.setup();
    const onAdd = vi.fn();
    render(<ProductCard product={product} onAdd={onAdd} />);
    await user.click(screen.getByRole('button', { name: /add to cart/i }));
    expect(onAdd).toHaveBeenCalledTimes(1);
    expect(onAdd).toHaveBeenCalledWith(product);
  });
});
