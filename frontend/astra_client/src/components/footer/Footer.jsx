export default function Footer() {
  return (
    <footer>
      <div className="footer-main">
        <div className="footer-brand">
          <div className="logo footer-logo"><span className="logo-mark">A</span><span>Astra</span></div>
          <p>Better Products. A Brighter Tomorrow.</p>
        </div>

        <div><h4>Quick Links</h4><a>About Us</a><a>Careers</a><a>Press</a><a>Blog</a></div>
        <div><h4>Customer Service</h4><a>Help Center</a><a>Track Order</a><a>Returns & Refunds</a><a>Contact Us</a></div>
        <div><h4>Policies</h4><a>Privacy Policy</a><a>Terms of Service</a><a>Shipping Policy</a><a>Return Policy</a></div>
        <div><h4>Follow Us</h4><div className="socials"><span>f</span><span>𝕏</span><span>◎</span><span>▶</span></div></div>
      </div>
      <div className="footer-bottom">
        <span>© 2026 Astra. All rights reserved.</span>
        <span>Visa · Mastercard · PayPal · Apple Pay · Google Pay</span>
      </div>
    </footer>
  );
}
