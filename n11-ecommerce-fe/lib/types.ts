export type Role = "CUSTOMER" | "ADMIN";

export type Session = {
  accessToken: string;
  refreshToken?: string;
  id: string;
  username: string;
  email: string;
  role: Role;
  firstName?: string;
  lastName?: string;
};

export type PageResponse<T> = {
  items?: T[];
  content?: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first?: boolean;
  last: boolean;
};

export type Category = {
  id: number;
  name: string;
  slug: string;
  description?: string;
  locale?: string;
  createdAt?: string;
  updatedAt?: string;
};

export type ProductSummary = {
  id: number;
  name: string;
  slug: string;
  price: string;
  imageUrl: string;
  active: boolean;
  locale?: string;
  category: Category;
};

export type Product = ProductSummary & {
  description: string;
  createdAt?: string;
  updatedAt?: string;
};

export type Cart = {
  id: number;
  userId: string;
  status: "ACTIVE" | "ORDERED" | "ABANDONED";
  items: CartItem[];
  totalPrice: string;
  lastActivityAt?: string;
  createdAt?: string;
  updatedAt?: string;
};

export type CartItem = {
  id: number;
  productId: number;
  productName: string;
  productImageUrl: string;
  unitPrice: string;
  quantity: number;
  lineTotal: string;
};

export type Address = {
  id: string;
  title: string;
  street: string;
  city: string;
  country: string;
  zipCode: string;
  defaultAddress: boolean;
  createdAt?: string;
  updatedAt?: string;
};

export type OrderStatus = "PENDING" | "CONFIRMED" | "SHIPPED" | "DELIVERED" | "CANCELLED";

export type Order = {
  id: number;
  orderNumber: string;
  userId: string;
  status: OrderStatus;
  statusReason?: string | null;
  totalPrice: string;
  paymentMethod: string;
  shippingAddress: {
    sourceAddressId: string;
    title: string;
    street: string;
    city: string;
    country: string;
    zipCode: string;
  };
  items: CartItem[];
  createdAt?: string;
  updatedAt?: string;
};

export type Inventory = {
  id: number;
  productId: number;
  availableQuantity: number;
  reservedQuantity: number;
  version: number;
  createdAt?: string;
  updatedAt?: string;
};

export type Payment = {
  id: number;
  orderId: number;
  orderNumber: string;
  userId: string;
  conversationId: string;
  iyzicoPaymentId: string | null;
  status: "PENDING" | "SUCCESS" | "FAILED";
  price: string;
  paidPrice: string;
  currency: string;
  iyzicoStatus: string | null;
  failureReason: string | null;
  items: {
    productId: number;
    productName: string;
    quantity: number;
    unitPrice: string;
    lineTotal: string;
  }[];
  createdAt?: string;
  updatedAt?: string;
};

export type User = {
  id: string;
  username: string;
  email: string;
  role: Role;
  firstName?: string;
  lastName?: string;
  phoneNumber?: string;
  addresses?: Address[];
  active: boolean;
  emailVerified: boolean;
  createdAt?: string;
  updatedAt?: string;
};
