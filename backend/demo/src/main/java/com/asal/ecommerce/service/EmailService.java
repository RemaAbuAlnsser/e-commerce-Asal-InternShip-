package com.asal.ecommerce.service;

import com.asal.ecommerce.dto.OrderResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url}")
    private String baseUrl;

    public void sendNewProductNotification(String toEmail, String productName,
                                           String categoryName, String price,
                                           String imageUrl, Long productId) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("New Arrival: " + productName + " is now available!");

            String productUrl = baseUrl + "/product/" + productId;

            String imageBlock = (imageUrl != null && !imageUrl.isBlank())
                    ? "<img src=\"" + imageUrl + "\" alt=\"" + productName + "\" "
                    + "style=\"width:100%;max-height:280px;object-fit:cover;border-radius:12px;margin-bottom:24px;\">"
                    : "";

            String html = """
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 32px 20px; background: #f8fafc;">
                      <div style="background: #fff; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.08);">

                        <!-- Header -->
                        <div style="background: linear-gradient(135deg, #4f46e5, #7c3aed); padding: 28px 32px; text-align: center;">
                          <p style="color: rgba(255,255,255,0.8); font-size: 13px; margin: 0 0 6px; text-transform: uppercase; letter-spacing: 1px;">New Arrival</p>
                          <h1 style="color: #fff; font-size: 24px; margin: 0; font-weight: 800;">Just Landed in Store ✨</h1>
                        </div>

                        <!-- Body -->
                        <div style="padding: 32px;">
                          %s
                          <p style="color: #6b7280; font-size: 13px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.8px; margin: 0 0 6px;">%s</p>
                          <h2 style="color: #111827; font-size: 22px; font-weight: 800; margin: 0 0 12px;">%s</h2>
                          <p style="color: #4f46e5; font-size: 26px; font-weight: 800; margin: 0 0 28px;">$%s</p>

                          <div style="text-align: center;">
                            <a href="%s"
                               style="display: inline-block; background: linear-gradient(135deg, #4f46e5, #7c3aed);
                                      color: #fff; text-decoration: none; padding: 14px 40px;
                                      border-radius: 10px; font-size: 16px; font-weight: 700;">
                              Shop Now →
                            </a>
                          </div>
                        </div>

                        <!-- Footer -->
                        <div style="background: #f8fafc; border-top: 1px solid #e5e7eb; padding: 20px 32px; text-align: center;">
                          <p style="color: #9ca3af; font-size: 12px; margin: 0;">
                            You're receiving this because you subscribed to our store updates.<br>
                            To unsubscribe, reply to this email.
                          </p>
                        </div>
                      </div>
                    </div>
                    """.formatted(imageBlock, categoryName, productName, price, productUrl);

            helper.setText(html, true);
            mailSender.send(message);

        } catch (Exception e) {
            // Log but don't crash — email failure must not affect product creation
            System.err.println("Failed to send new-product email to " + toEmail + ": " + e.getMessage());
        }
    }

    public void sendLoginEmail(String toEmail, String name, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Your sign-in link");

            String loginUrl = baseUrl + "/subscriber-login?token=" + token;

            String html = """
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 40px 20px;">
                      <div style="background: linear-gradient(135deg, #4f46e5, #7c3aed); border-radius: 16px; padding: 40px; text-align: center; color: white; margin-bottom: 30px;">
                        <h1 style="margin: 0 0 8px 0; font-size: 28px;">Welcome back, %s! 👋</h1>
                        <p style="margin: 0; opacity: 0.9; font-size: 16px;">Click the button below to sign in instantly</p>
                      </div>
                      <div style="background: #f8fafc; border-radius: 12px; padding: 30px; margin-bottom: 24px;">
                        <p style="color: #374151; font-size: 16px; margin: 0 0 24px 0;">
                          Use this magic link to sign in to your account. The link expires in 15 minutes.
                        </p>
                        <div style="text-align: center;">
                          <a href="%s"
                             style="display: inline-block; background: linear-gradient(135deg, #4f46e5, #7c3aed); color: white; text-decoration: none;
                                    padding: 14px 36px; border-radius: 8px; font-size: 16px; font-weight: 600;">
                            Sign In Now →
                          </a>
                        </div>
                      </div>
                      <p style="color: #9ca3af; font-size: 13px; text-align: center; margin: 0;">
                        If you didn't request this, you can safely ignore this email.
                      </p>
                    </div>
                    """.formatted(name, loginUrl);

            helper.setText(html, true);
            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send login email: " + e.getMessage(), e);
        }
    }

    @Async
    public void sendOrderConfirmationEmail(OrderResponse order) {
        if (order.getCustomerEmail() == null || order.getCustomerEmail().isBlank()) return;
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(order.getCustomerEmail());
            helper.setSubject("Order Confirmed – #" + order.getId());

            String itemsHtml = buildItemsHtml(order);

            String html = """
                    <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:32px 20px;background:#f8fafc;">
                      <div style="background:#fff;border-radius:16px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.08);">
                        <div style="background:linear-gradient(135deg,#4f46e5,#7c3aed);padding:28px 32px;text-align:center;">
                          <p style="color:rgba(255,255,255,0.8);font-size:13px;margin:0 0 6px;text-transform:uppercase;letter-spacing:1px;">Order Confirmation</p>
                          <h1 style="color:#fff;font-size:24px;margin:0;font-weight:800;">Thank you, %s! 🎉</h1>
                        </div>
                        <div style="padding:32px;">
                          <p style="color:#374151;font-size:15px;margin:0 0 20px;">
                            Your order <strong>#%d</strong> has been placed successfully. We'll contact you shortly to confirm delivery.
                          </p>
                          %s
                          <table style="width:100%%;border-top:1px solid #e5e7eb;margin-top:16px;padding-top:16px;">
                            <tr><td style="color:#6b7280;padding:6px 0;">Shipping to</td><td style="text-align:right;font-weight:600;color:#111827;">%s, %s</td></tr>
                            <tr><td style="color:#6b7280;padding:6px 0;">Payment</td><td style="text-align:right;font-weight:600;color:#111827;">Cash on Delivery</td></tr>
                            <tr><td style="color:#6b7280;padding:6px 0;">Delivery fee</td><td style="text-align:right;font-weight:600;color:#111827;">$%.2f</td></tr>
                            <tr style="border-top:1px solid #e5e7eb;">
                              <td style="color:#111827;font-size:16px;font-weight:800;padding:12px 0 0;">Total</td>
                              <td style="text-align:right;font-size:16px;font-weight:800;color:#4f46e5;padding-top:12px;">$%.2f</td>
                            </tr>
                          </table>
                        </div>
                        <div style="background:#f8fafc;border-top:1px solid #e5e7eb;padding:20px 32px;text-align:center;">
                          <p style="color:#9ca3af;font-size:12px;margin:0;">Questions? Reply to this email and we'll help you out.</p>
                        </div>
                      </div>
                    </div>
                    """.formatted(
                    order.getCustomerName(),
                    order.getId(),
                    itemsHtml,
                    order.getCustomerAddress(), order.getCustomerCity(),
                    order.getShippingCost().doubleValue(),
                    order.getTotal().doubleValue()
            );

            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send order confirmation email: " + e.getMessage());
        }
    }

    @Async
    public void sendOrderStatusUpdateEmail(OrderResponse order) {
        if (order.getCustomerEmail() == null || order.getCustomerEmail().isBlank()) return;
        try {
            String status = order.getStatus();
            String emoji, headline, detail, bannerColor;

            switch (status.toLowerCase()) {
                case "confirmed" -> {
                    emoji = "✅"; headline = "Order Confirmed!";
                    detail = "Great news! Your order has been confirmed and is being prepared.";
                    bannerColor = "#10b981";
                }
                case "shipped" -> {
                    emoji = "🚚"; headline = "Your Order is On Its Way!";
                    detail = "Your order has been shipped and is heading to you.";
                    bannerColor = "#3b82f6";
                }
                case "delivered" -> {
                    emoji = "📦"; headline = "Order Delivered!";
                    detail = "Your order has been delivered. We hope you enjoy your purchase!";
                    bannerColor = "#4f46e5";
                }
                case "cancelled" -> {
                    emoji = "❌"; headline = "Order Cancelled";
                    detail = "Your order has been cancelled. If you have questions, please contact us.";
                    bannerColor = "#ef4444";
                }
                default -> {
                    emoji = "📋"; headline = "Order Update";
                    detail = "Your order status has been updated to: " + status + ".";
                    bannerColor = "#6b7280";
                }
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(order.getCustomerEmail());
            helper.setSubject(emoji + " Order #" + order.getId() + " – " + headline);

            String html = """
                    <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:32px 20px;background:#f8fafc;">
                      <div style="background:#fff;border-radius:16px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.08);">
                        <div style="background:%s;padding:28px 32px;text-align:center;">
                          <p style="color:rgba(255,255,255,0.85);font-size:13px;margin:0 0 6px;text-transform:uppercase;letter-spacing:1px;">Order #%d</p>
                          <h1 style="color:#fff;font-size:26px;margin:0;font-weight:800;">%s %s</h1>
                        </div>
                        <div style="padding:32px;">
                          <p style="color:#374151;font-size:15px;line-height:1.6;margin:0 0 20px;">
                            Hi <strong>%s</strong>, %s
                          </p>
                          <div style="background:#f8fafc;border-radius:10px;padding:16px 20px;margin-bottom:20px;">
                            <p style="margin:0;font-size:13px;color:#6b7280;">Delivery address</p>
                            <p style="margin:4px 0 0;font-weight:600;color:#111827;">%s, %s</p>
                          </div>
                        </div>
                        <div style="background:#f8fafc;border-top:1px solid #e5e7eb;padding:20px 32px;text-align:center;">
                          <p style="color:#9ca3af;font-size:12px;margin:0;">Questions? Reply to this email and we'll help you out.</p>
                        </div>
                      </div>
                    </div>
                    """.formatted(
                    bannerColor,
                    order.getId(),
                    emoji, headline,
                    order.getCustomerName(), detail,
                    order.getCustomerAddress(), order.getCustomerCity()
            );

            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send order status email: " + e.getMessage());
        }
    }

    private String buildItemsHtml(OrderResponse order) {
        if (order.getItems() == null || order.getItems().isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        sb.append("<table style=\"width:100%;border-collapse:collapse;margin-bottom:16px;\">");
        for (var item : order.getItems()) {
            sb.append("""
                    <tr style="border-bottom:1px solid #f3f4f6;">
                      <td style="padding:10px 0;color:#111827;font-weight:600;">%s%s</td>
                      <td style="padding:10px 0;text-align:right;color:#374151;">x%d</td>
                      <td style="padding:10px 0;text-align:right;font-weight:700;color:#4f46e5;">$%.2f</td>
                    </tr>
                    """.formatted(
                    item.getProductName(),
                    item.getColorName() != null ? " (" + item.getColorName() + ")" : "",
                    item.getQuantity(),
                    item.getSubtotal().doubleValue()
            ));
        }
        sb.append("</table>");
        return sb.toString();
    }

    public void sendVerificationEmail(String toEmail, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Confirm your subscription");

            String verifyUrl = baseUrl + "/verify-email?token=" + token;

            String html = """
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 40px 20px;">
                      <div style="background: linear-gradient(135deg, #4f46e5, #7c3aed); border-radius: 16px; padding: 40px; text-align: center; color: white; margin-bottom: 30px;">
                        <h1 style="margin: 0 0 8px 0; font-size: 28px;">You're almost in! 🎉</h1>
                        <p style="margin: 0; opacity: 0.9; font-size: 16px;">Confirm your email to complete your subscription</p>
                      </div>
                      <div style="background: #f8fafc; border-radius: 12px; padding: 30px; margin-bottom: 24px;">
                        <p style="color: #374151; font-size: 16px; margin: 0 0 24px 0;">
                          Thank you for subscribing! Click the button below to verify your email address and start receiving our latest updates, new arrivals, and exclusive offers.
                        </p>
                        <div style="text-align: center;">
                          <a href="%s"
                             style="display: inline-block; background: linear-gradient(135deg, #4f46e5, #7c3aed); color: white; text-decoration: none;
                                    padding: 14px 36px; border-radius: 8px; font-size: 16px; font-weight: 600;">
                            Verify My Email
                          </a>
                        </div>
                      </div>
                      <p style="color: #9ca3af; font-size: 13px; text-align: center; margin: 0;">
                        If you didn't subscribe, you can safely ignore this email.<br>
                        This link expires in 24 hours.
                      </p>
                    </div>
                    """.formatted(verifyUrl);

            helper.setText(html, true);
            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send verification email: " + e.getMessage(), e);
        }
    }
}
