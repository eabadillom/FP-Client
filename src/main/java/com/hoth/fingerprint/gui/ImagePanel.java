package com.hoth.fingerprint.gui;

import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Fid.Fiv;

import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class ImagePanel extends JPanel 
{
    private static final long serialVersionUID = 5L;
    private BufferedImage image;
    private int width, height;

    public void showImage(Fid fidImage) 
    {
        Fiv view = fidImage.getViews()[0];
        width = view.getWidth();
        height = view.getHeight();

        image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        image.getRaster().setDataElements(0, 0, width, height, view.getImageData());
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) 
    {
        super.paintComponent(g);
        if (image != null) {
            int x = (getWidth() - width) / 2;
            int y = (getHeight() - height) / 2;
            g.drawImage(image, x, y, this);
        }
    }
    
}
